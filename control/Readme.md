As per the feedback last Friday 23/09, I took a different approach to write it using Golang framework operator SDK. This framework provides much
higher level of abstraction when it comes to calling kubernetes APIs & also I looked up some sample Go codes online related to operator sdk & the implementation using this
framework appeared to be much simpler. 

First step was to install operator sdk in the codespace ubuntu instance I was using, for the same. The prerequisite 
to install opeator SDK is to have Go version 1.21+ installed. 

I created a new Custom Resource Definition(CRD) for this with couple of custom properties, i.e. image & replicas. So that the image tag & number
of replicas can be changed with the custom resources yaml. The actual CRD file is present in the following location.
CRD ===> https://github.com/Kaniska10/myownspace/blob/master/control/crds/app.mydomain.com_myresources.yaml 
Custom resource ===> https://github.com/Kaniska10/myownspace/blob/master/control/crds/myresource.yaml

The go programme is in the following location:
https://github.com/Kaniska10/myownspace/blob/master/control/controllers/myresource_controller.go
It works based on the Reconcile function such that it checks & creates the intended pod if not already running.

The inner script is same as the one as shared on Friday.
https://github.com/Kaniska10/myownspace/blob/master/control/script/loop_request.sh 

Currently I am encountering an issue with the deployment of the main controller which is probably because ofincorrect config or permission problem to call kubernetes API which is required to be checked further. 

Updates for 24/09 ====>
--------------------------
Yesterday I was facing issue while deploying the operator sdk. While checking this further today, I noticed some issues with the main crd yaml file, corected them & uploaded the latest version.
CRD ===> https://github.com/Kaniska10/myownspace/blob/master/control/crds/app.mydomain.com_myresources.yaml

There was a further problem in deploying the sdk & kubectl command not able to connect to kubernetes APIs so to solve that I setup minikube in codespace instance which would be used by dafault for kubectl & the cluster would be created on top of minikube vm. This solved the deployment issue finally.

Then applied both CRD & CR with the following kubectl apply commands but still the controllers & inner application pods aren't up & running. 
kubectl apply -f app.mydomain.com_myresources.yaml
kubectl apply -f myresource.yaml

I verified that custom resource was created all fine. Didn't find any issue in the controller-manager pod log either, as there I only see the controller manager startup log statements. I had initially thought that perhaps the myresource_controller.go file written for the reconciler has to be explicitly invoked from main.go inside the operator sdk package but then while checking the main.go I saw that the reconciler function is already invoked during the startup of the manager, so should ideally be picked automatically.

Also I checked permissions which are already bundled as part of the operator sdk package in config/rbac location & they look fine. So this issue would require further investigation. 

Updates for 25/09 ====>
--------------------------
Today when I started looking into it the main problem was that the application pods were not coming up & from the log it was evident that even the reconciler itself wasn't running. The other problem was that while deploying the operator sdk controller manager, the main CRD file was getting replaced with a basic one due to which the CRD wasn't getting created with desired fields.

I found that a template code was prsent in the package in api/v1 location named myresource_types.go which had to be modified to include the required fields & it would further generate the CRD. I added the image & replicas fields which I wanted for my CR.
The file is in location ===> https://github.com/Kaniska10/myownspace/blob/master/control/api/v1/myresource_types.go 
The updated CRD in location ===> https://github.com/Kaniska10/myownspace/blob/master/control/crds/app.mydomain.com_myresources.yaml 

Then for the main issue on why the custom code written for controller wasn't getting picked, I saw in the main Dockerfile that in the package while creating the build, its copying a default file with the same name in a internal/controller location, so I placed my changes in that file & there were few mistakes in my code corrected them.
The code is available in location ===> https://github.com/Kaniska10/myownspace/blob/master/control/controllers/myresource_controller.go 

After this while I deployed the manager, faced some permission issues as shown below.

2024-09-25T13:24:36Z    INFO    Starting Controller     {"controller": "myresource", "controllerGroup": "app.mydomain.com", "controllerKind": "MyResource"}
W0925 13:24:36.795652       1 reflector.go:539] pkg/mod/k8s.io/client-go@v0.29.2/tools/cache/reflector.go:229: failed to list *v1.Pod: pods is forbidden: User "system:serviceaccount:mysdk-system:mysdk-controller-manager" cannot list resource "pods" in API group "" at the cluster scope
E0925 13:24:36.795696       1 reflector.go:147] pkg/mod/k8s.io/client-go@v0.29.2/tools/cache/reflector.go:229: Failed to watch *v1.Pod: failed to list *v1.Pod: pods is forbidden: User "system:serviceaccount:mysdk-system:mysdk-controller-manager" cannot list resource "pods" in API group "" at the cluster scope

And

2024-09-25T13:33:44Z    ERROR   Reconciler error        {"controller": "myresource", "controllerGroup": "app.mydomain.com", "controllerKind": "MyResource", "MyResource": {"name":"myresource-sample","namespace":"default"}, "namespace": "default", "name": "myresource-sample", "reconcileID": "710d17fd-4eab-466a-aa39-0c3a0eebd170", "error": "pods is forbidden: User \"system:serviceaccount:mysdk-system:mysdk-controller-manager\" cannot create resource \"pods\" in API group \"\" in the namespace \"default\""}
sigs.k8s.io/controller-runtime/pkg/internal/controller.(*Controller).reconcileHandler
        /go/pkg/mod/sigs.k8s.io/controller-runtime@v0.17.3/pkg/internal/controller/controller.go:329
sigs.k8s.io/controller-runtime/pkg/internal/controller.(*Controller).processNextWorkItem
        /go/pkg/mod/sigs.k8s.io/controller-runtime@v0.17.3/pkg/internal/controller/controller.go:266
sigs.k8s.io/controller-runtime/pkg/internal/controller.(*Controller).Start.func2.2
        /go/pkg/mod/sigs.k8s.io/controller-runtime@v0.17.3/pkg/internal/controller/controller.go:227

These were mainly because the default service account "mysdk-controller-manager" created for the controller & application pods in "default" namespace didn't have required permissions. So added them manually.
These permission files present in location ===> https://github.com/Kaniska10/myownspace/tree/master/control/rbac 

Then finally it worked. Updated CR file is in location ===> https://github.com/Kaniska10/myownspace/blob/master/control/crds/app_v1_myresource.yaml 

Log after successful creation below ===> https://github.com/Kaniska10/myownspace/blob/master/control/capture.txt 

