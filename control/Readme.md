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

I verified that custom resource was created all fine. Didn't find any issue controller-manager pod log either, as there I only see the controller manager startup log statements. I had initially thought that perhaps the myresource_controller.go file written for the reconciler has to be explicitly invoked from main.go inside the operator sdk package but then while checking the main.go I saw that the reconciler function is already invoked during the startup of the manager, so should ideally be picked automatically.

Also I checked permissions which are already bundled as part of the operator sdk package in config/rbac location & they look fine. SO this issue would require further investigation. 


