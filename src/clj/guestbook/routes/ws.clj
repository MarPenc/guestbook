(ns guestbook.routes.ws
  (:require [compojure.core :refer [GET POST defroutes]]
            [mount.core :refer [defstate]]
            [guestbook.db.core :as db]
            [guestbook.validation :as v]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.immutant
             :refer [sente-web-server-adapter]]))

(let [connection (sente/make-channel-socket!
                  sente-web-server-adapter
                  {:user-id-fn
                   (fn [ring-req] (get-in ring-req [:params :client-id]))})]

  (def ring-ajax-post
    "The function that handles ajax POST requests"
    (:ajax-post-fn connection))

  (def ring-ajax-get-or-ws-handshake
    "The function that negotiates the initial connection.
     That is: falling back on ajax if websocket is not an option"
    (:ajax-get-or-ws-handshake-fn connection))

  (def ch-chsk
    "The receive channel for the socket"
    (:ch-recv connection))

  (def chsk-send!
    "The function used to send push notifications to the client"
    (:send-fn connection))

  (def connected-uids
    "An atom containing the ids of connected clients.
     `sente` defaults to using session-ids. Since we not yet provide sessions
     we use this to identify the client."
    (:connected-uids connection)))

(defn save-message!
  "Validates the given message and returns the occuring errors or stores
   the message otherwise."
  [message]
  (if-let [errors (v/validate-message message)]
    {:errors errors}
    (do
      (db/save-message! message)
      message)))

(defn handle-message!
  "Checks for connection and response with an error message to the issuing
   client or with the new message to all clients."
  [{:keys [id client-id ?data]}]
  (when (= id :guestbook/add-message)
    (let [response (-> ?data
                       (assoc :timestamp (java.util.Date.))
                       save-message!)]
      (if (:errors response)
        (chsk-send! client-id (:guestbook/error response))
        (doseq [uid (:any @connected-uids)]
          (chsk-send! uid [:guestbook/add-message response]))))))

(defn stop-router! [stop-fn]
  (when stop-fn (stop-fn)))

(defn start-router! []
  (sente/start-chsk-router! ch-chsk handle-message!))

(defstate router
  :start (start-router!)
  :stop (stop-router! router))

(defroutes websocket-routes
  (GET  "/ws" req (ring-ajax-get-or-ws-handshake req))
  (POST "/ws" req (ring-ajax-post req)))
