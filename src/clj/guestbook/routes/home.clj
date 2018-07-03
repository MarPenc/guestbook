(ns guestbook.routes.home
  (:require [guestbook.layout :as layout]
            [guestbook.db.core :as db]
            [guestbook.routes.ws :as ws]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as r]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET  "/"          []   (home-page))
  (GET  "/messages"  []   (r/ok (db/get-messages)))
  (POST "/message"   req  (ws/save-message! req)))
