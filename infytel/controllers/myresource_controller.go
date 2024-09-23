package controllers

import (
    "context"
    "fmt"
    "github.com/go-logr/logr"
    appv1 "github.com/myuser/my-operator/api/v1"
    corev1 "k8s.io/api/core/v1"
    "k8s.io/apimachinery/pkg/api/errors"
    "k8s.io/apimachinery/pkg/types"
    "sigs.k8s.io/controller-runtime/pkg/controller/controllerutil"
    "sigs.k8s.io/controller-runtime/pkg/controller"
    "sigs.k8s.io/controller-runtime/pkg/reconcile"
    "sigs.k8s.io/controller-runtime/pkg/client"
    "sigs.k8s.io/controller-runtime/pkg/log"
    "sigs.k8s.io/controller-runtime/pkg/manager"
)

// MyResourceReconciler reconciles a MyResource object
type MyResourceReconciler struct {
    client.Client
    Scheme *runtime.Scheme
}

// Reconcile reads that state of the cluster for a MyResource object
func (r *MyResourceReconciler) Reconcile(req reconcile.Request) (reconcile.Result, error) {
    ctx := context.Background()
    log := log.FromContext(ctx)

    var myResource appv1.MyResource
    if err := r.Get(ctx, req.NamespacedName, &myResource); err != nil {
        if errors.IsNotFound(err) {
            log.Info("MyResource resource not found. Ignoring since object must be deleted")
            return reconcile.Result{}, nil
        }
        log.Error(err, "Failed to get MyResource")
        return reconcile.Result{}, err
    }

    // Define a new Pod
    pod := r.newPod(&myResource)
    if err := controllerutil.SetControllerReference(&myResource, pod, r.Scheme); err != nil {
        return reconcile.Result{}, err
    }

    // Check if the Pod already exists
    found := &corev1.Pod{}
    err := r.Get(ctx, types.NamespacedName{Name: pod.Name, Namespace: pod.Namespace}, found)
    if err != nil && errors.IsNotFound(err) {
        log.Info("Creating a new Pod", "Pod.Namespace", pod.Namespace, "Pod.Name", pod.Name)
        err = r.Create(ctx, pod)
        if err != nil {
            log.Error(err, "Failed to create new Pod", "Pod.Namespace", pod.Namespace, "Pod.Name", pod.Name)
            return reconcile.Result{}, err
        }
        // Pod created successfully - return and requeue
        return reconcile.Result{Requeue: true}, nil
    } else if err != nil {
        log.Error(err, "Failed to get Pod")
        return reconcile.Result{}, err
    }

    log.Info("Pod already exists", "Pod.Namespace", found.Namespace, "Pod.Name", found.Name)
    return reconcile.Result{}, nil
}

// newPod returns a pod with the same name/namespace as the cr
func (r *MyResourceReconciler) newPod(cr *appv1.MyResource) *corev1.Pod {
    return &corev1.Pod{
        ObjectMeta: metav1.ObjectMeta{
            Name:      cr.Name,
            Namespace: cr.Namespace,
        },
        Spec: corev1.PodSpec{
            Containers: []corev1.Container{
                {
                    Name:  "my-container",
                    Image: cr.Spec.Image,
                },
            },
        },
    }
}

// SetupWithManager sets up the controller with the Manager
func (r *MyResourceReconciler) SetupWithManager(mgr ctrl.Manager) error {
    return ctrl.NewControllerManagedBy(mgr).
        For(&appv1.MyResource{}).
        Owns(&corev1.Pod{}).
        Complete(r)
}
