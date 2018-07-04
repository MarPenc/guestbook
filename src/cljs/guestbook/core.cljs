(ns guestbook.core
  (:require [reagent.core :as r :refer [atom]]
            [ajax.core :refer [GET]]
            [guestbook.ws :as ws]))

(defn message-list [messages]
  [:ul.content
   (for [{:keys [timestamp message name]} (reverse @messages)]
     ^{:key timestamp}
     [:li
      [:div.panel.panel-white.post
       [:div.post-heading
        [:div.title.h5 name " wrote:"]
        [:time.h6 (.toLocaleString timestamp)]]
       [:div.post-description
        [:p message]
        [:div.stats
         [:button.btn.btn-info.stat-item
          [:i.fa.fa-thumbs-up.icon] 32]
         [:button.btn.btn-secondary.stat-item
          [:i.fa.fa-thumbs-down.icon] 2]]]]])])


(defn get-messages [messages]
  (GET "/messages"
    {:headers {"Accept" "application/transit+json"}
     :handler #(reset! messages (vec %))}))

(defn errors-component [errors id]
  (when-let [error (id @errors)]
    [:div.alert.alert-danger (clojure.string/join error)]))

(defn response-handler [messages fields errors]
  (fn [{[_ message] :?data}]
    (if-let [response-errors (:errors message)]
      (reset! errors response-errors)
      (do
        (reset! errors nil)
        (reset! fields nil)
        (swap! messages conj message)))))

(defn message-form [fields errors]
  [:div.content
   [:div.form-group
    [errors-component errors :server-error]
    [:p
     [:input.form-control
      {:type :text
       :placeholder "Your Name"
       :value (:name @fields)
       :on-change #(swap! fields assoc :name (-> % .-target .-value))}]
     [errors-component errors :name]]
    [:p
     [:textarea.form-control
      {:rows 5
       :cols 50
       :placeholder "What's going on?"
       :value (:message @fields)
       :on-change #(swap! fields assoc :message (-> % .-target .-value))}]
     [errors-component errors :message]]
    [:input.btn.btn-info.pull-right.btn-comment
     {:type :submit
      :on-click #(ws/send-message! [:guestbook/add-message @fields] 8000)
      :value "Comment"}]]])

(defn home []
  (let [messages (atom nil)
        errors   (atom nil)
        fields   (atom nil)]
    (ws/start-router! (response-handler messages fields errors))
    (get-messages messages)
    (fn []
      [:div
        [:h2 "Shout!"]
        [:p "your personal shoutbox"]
        [:div.row
          [:div.span12
            [message-form fields errors]]]
        [:div.row
          [:div.span12
            [message-list messages]]]])))

(r/render [home]
  (.getElementById js/document "app"))
