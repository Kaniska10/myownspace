package com.person.control;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;

import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import com.google.gson.JsonElement;
import com.person.control.models.V1NewCrd;
import com.person.control.models.V1NewCrdList;
import com.person.control.models.V1NewCrdSpec;
import com.person.control.models.V1NewCrdStatus;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.extended.controller.builder.DefaultControllerBuilder;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import io.kubernetes.client.openapi.models.V1Volume;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import io.kubernetes.client.util.Yaml;
import io.kubernetes.client.util.generic.GenericKubernetesApi;


@SpringBootApplication
public class ControlApplication {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ControlApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(ControlApplication.class, args);
	}
	
    @Bean
    GenericKubernetesApi<V1NewCrd, V1NewCrdList> foosApi(ApiClient apiClient) {
        return new GenericKubernetesApi<>(V1NewCrd.class, V1NewCrdList.class, "com.person.control", "v1", "new-crds", apiClient);
    }
    
    @Bean
    GenericKubernetesApi<V1Deployment, V1DeploymentList> deploymentsApi(ApiClient apiClient) {
        return new GenericKubernetesApi<>(V1Deployment.class, V1DeploymentList.class, "", "v1", "deployments", apiClient);
    }
    
	@Bean
	SharedIndexInformer<V1NewCrd> foosSharedIndexInformer(SharedInformerFactory sharedInformerFactory,
			GenericKubernetesApi<V1NewCrd, V1NewCrdList> api) {
		return sharedInformerFactory.sharedIndexInformerFor(api, V1NewCrd.class, 0);
	}
	
	@Bean
	AppsV1Api appsV1Api(ApiClient apiClient) {
		return new AppsV1Api(apiClient);
	}

	@Bean
	CoreV1Api coreV1Api(ApiClient apiClient) {
		return new CoreV1Api(apiClient);
	}
	
	@Bean
	Controller controller(SharedInformerFactory sharedInformerFactory, SharedIndexInformer<V1NewCrd> crdNodeInformer,
			Reconciler reconciler) {

		DefaultControllerBuilder builder = ControllerBuilder //
				.defaultBuilder(sharedInformerFactory)//
				.watch(crdQ -> ControllerBuilder //
						.controllerWatchBuilder(V1NewCrd.class, crdQ)//
						.withResyncPeriod(Duration.ofSeconds(1))//
						.build()) //
				.withWorkerCount(2);
		return builder//
				.withReconciler(reconciler) //
				.withReadyFunc(crdNodeInformer::hasSynced) // optional: only start once
															// the index is synced
				.withName("crdController") ///
				.build();

	}
	
	@Bean
	ApplicationRunner runner(SharedInformerFactory sharedInformerFactory, Controller controller) {
		var executorService = Executors.newCachedThreadPool();
		return args -> executorService.execute(() -> {
			sharedInformerFactory.startAllRegisteredInformers();
			controller.run();
		});
	}
	
	@FunctionalInterface
	interface ApiSupplier<T> {

		T get() throws ApiException;

	}
	
	/**
	 * the Reconciler won't get an event telling it that the cluster has changed, but
	 * instead it looks at cluster state and determines that something has changed
	 */
	@Bean
	Reconciler reconciler(@Value("classpath:configmap.yaml") Resource configMapYaml,
			@Value("classpath:deployment.yaml") Resource deploymentYaml,
			SharedIndexInformer<V1NewCrd> v1FooSharedIndexInformer, AppsV1Api appsV1Api, CoreV1Api coreV1Api) {
		return request -> {
			try {
				// create new one on k apply -f foo.yaml
				String requestName = request.getName();
				String key = request.getNamespace() + '/' + requestName;
				V1NewCrd crd = v1FooSharedIndexInformer.getIndexer().getByKey(key);
				if (crd == null) { // deleted. we use ownerreferences so dont need to do
									// anything special here
					return new Result(false);
				}

				String namespace = crd.getMetadata().getNamespace();
				String pretty = "true";
				String dryRun = null;
				String fieldManager = "";
				String fieldValidation = "";

				// parameterize configmap
				String configMapName = "configmap-" + requestName;
				V1ConfigMap configMap = loadYamlAs(configMapYaml, V1ConfigMap.class);
				String html = "<h1> Hello, " + crd.getSpec().getName() + " </h1>";
				configMap.getData().put("index.html", html);
				configMap.getMetadata().setName(configMapName);
				String deploymentName = "deployment-" + requestName;
				createOrUpdate(V1ConfigMap.class, () -> {
					addOwnerReference(requestName, crd, configMap);
					return coreV1Api.createNamespacedConfigMap(namespace, configMap, pretty, dryRun, fieldManager,
							fieldValidation);
				}, () -> {
					V1ConfigMap v1ConfigMap = coreV1Api.replaceNamespacedConfigMap(configMapName, namespace, configMap,
							pretty, dryRun, fieldManager, fieldValidation);
					// todo now we need to add an annotation to the deployment

					return v1ConfigMap;
				});

				// parameterize deployment
				V1Deployment deployment = loadYamlAs(deploymentYaml, V1Deployment.class);
				deployment.getMetadata().setName(deploymentName);
				List<V1Volume> volumes = deployment.getSpec().getTemplate().getSpec().getVolumes();
				Assert.isTrue(volumes.size() == 1, () -> "there should be only one V1Volume");
				volumes.forEach(vol -> vol.getConfigMap().setName(configMapName));
				createOrUpdate(V1Deployment.class, () -> {
					deployment.getSpec().getTemplate().getMetadata()
							.setAnnotations(Map.of("controller-update", Instant.now().toString()));
					addOwnerReference(requestName, crd, deployment);
					return appsV1Api.createNamespacedDeployment(namespace, deployment, pretty, dryRun, fieldManager,
							fieldValidation);
				}, () -> {
					updateAnnotation(deployment);
					return appsV1Api.replaceNamespacedDeployment(deploymentName, namespace, deployment, pretty, dryRun,
							fieldManager, fieldValidation);
				});
			} //
			catch (Throwable e) {
				log.error("we've got an outer error.", e);
				return new Result(true, Duration.ofSeconds(60));
			}
			return new Result(false);
		};
	}
	
	@SneakyThrows
	private static <T> T loadYamlAs(Resource resource, Class<T> wrapper) throws IOException {
		var yaml = FileCopyUtils.copyToString(new InputStreamReader(resource.getInputStream()));
		return Yaml.loadAs(yaml, wrapper);
	}
	
	private void updateAnnotation(V1Deployment deployment) {
		deployment.getSpec().getTemplate().getMetadata()
				.setAnnotations(Map.of("controller-update", Instant.now().toString()));
	}
	
	static private <T> void createOrUpdate(Class<T> clazz, ApiSupplier<T> creator, ApiSupplier<T> updater) {
		try {
			creator.get();
			log.info("It worked! we created a new " + clazz.getName() + "!");
		} //
		catch (ApiException throwable) {
			int code = throwable.getCode();
			if (code == 409) { // resource already existing
				log.info("the " + clazz.getName() + " already exists. Going to replace.");
				try {
					updater.get();
					log.info("successfully updated the " + clazz.getName());
				}
				catch (ApiException ex) {
					log.error("got an error on update", ex);
				}
			} //
			else {
				log.info("got an exception with code " + code + " while trying to create the " + clazz.getName());
			}
		}
	}
	
	private static V1ObjectMeta addOwnerReference(String requestName, V1NewCrd foo, KubernetesObject kubernetesObject) {
		Assert.notNull(foo, () -> "the V1NewCrd must not be null");
		return kubernetesObject.getMetadata().addOwnerReferencesItem(new V1OwnerReference().kind(foo.getKind())
				.apiVersion(foo.getApiVersion()).controller(true).uid(foo.getMetadata().getUid()).name(requestName));
	}

}
