(ns guestbook.handler
  (:require [guestbook.layout :refer [error-page]]
            [guestbook.routes.home :refer [home-routes]]
            [guestbook.routes.ws :refer [websocket-routes]]
            [compojure.core :refer [routes wrap-routes]]
            [guestbook.middleware :as middleware]
            [compojure.route :as route]
            [guestbook.env :refer [defaults]]
            [mount.core :as mount]))

(mount/defstate init-app
  :start ((or (:init defaults) identity))
  :stop  ((or (:stop defaults) identity)))

(mount/defstate app
  :start
  (middleware/wrap-base
    (routes ;; (defn routes [& handlers] ...)
      websocket-routes
      (-> #'home-routes
          (wrap-routes middleware/wrap-csrf)
          (wrap-routes middleware/wrap-formats))
      (route/not-found
         (:body
           (error-page {:status 404
                        :title "page not found"}))))))
