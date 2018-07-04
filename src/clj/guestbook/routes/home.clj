(ns guestbook.routes.home
  (:require [guestbook.layout :as layout]
            [guestbook.db.core :as db]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as r]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET  "/"         [] (home-page))
  (GET  "/messages" [] (r/ok (db/get-messages))))
