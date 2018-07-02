(ns guestbook.validation
  (:require [struct.core :as st]))

(def message-schema
  [[:name st/required st/string]
   [:message st/required st/string
    {:message "Messages must be at least 5 characters long."
     :validate #(> (count %) 4)}]])

(defn validate-message [params]
  (first (st/validate params message-schema)))
