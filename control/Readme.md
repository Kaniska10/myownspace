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
