(ns guestbook.routes.home
  (:require [guestbook.layout :as layout]
            [guestbook.db.core :as db]
            [guestbook.validation :as validation]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as r]
            [ring.util.response :refer [response status]]))

(defn save-message! [{:keys [params]}]
  (if-let [errors (validation/validate-message params)]
    (r/bad-request {:errors errors})
    (try
      (db/save-message! (assoc params :timestamp (java.util.Date.)))
      (r/ok {:status :ok})
      (catch Exception e
        (r/internal-server-error
         {:errors {:server-error ["Failed to save message!"]}})))))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET  "/"          []   (home-page))
  (GET  "/messages"  []   (r/ok (db/get-messages)))
  (POST "/message"   req  (save-message! req)))
