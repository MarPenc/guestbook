(ns guestbook.core
  (:require [reagent.core :as r :refer [atom]]
            [ajax.core :refer [GET POST]]
            [clojure.string :refer [join]]))

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

(defn send-message! [fields errors messages]
  (POST "/message"
    {:headers {"Accept" "application/transit+json"
               "x-csrf-token" (.-value (.getElementById js/document "token"))}
     :params @fields
     :handler #(do
                 (reset! errors nil)
                 (swap! messages conj (assoc @fields :timestamp (js/Date.))))
     :error-handler #(do
                       (.error js/console (str "error: " %))
                       (reset! errors (get-in % [:response :errors])))}))

(defn errors-component [errors id]
  (when-let [error (id @errors)]
    [:div.alert.alert-danger (join error)]))

(defn message-form [messages]
  (let [fields (atom {})
        errors (atom nil)]
    (fn []
      [:div.content
       [errors-component errors :server-error]
       [:div.form-group

        [:p
         [:input.form-control
          {:type :text
           :name :name
           :placeholder "Your Name"
           :value (:name @fields)
           :on-change #(swap! fields assoc :name (-> % .-target .-value))}]
         [errors-component errors :name]]

        [:p
         [:textarea.form-control
          {:rows 5
           :cols 50
           :placeholder "What's going on?"
           :name :message
           :value (:message @fields)
           :on-change #(swap! fields assoc :message (-> % .-target .-value))}]
         [errors-component errors :message]]

        [:input.btn.btn-info.pull-right.btn-comment
         {:type :submit
          :on-click #(send-message! fields errors messages)
          :value "Comment"}]]])))

(defn home []
  (let [messages (atom nil)]
    (get-messages messages)
    (fn []
      [:div
        [:h2 "Shout!"]
        [:p "your personal shoutbox"]
        [:div.row
          [:div.col-sm-5
            [message-form messages]]
          [:div.col-sm-5
            [message-list messages]]]])))

(r/render [home]
  (.getElementById js/document "app"))
