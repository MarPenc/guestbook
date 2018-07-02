(ns ^:figwheel-no-load guestbook.app
  (:require [guestbook.core :as core]
            [cljs.spec.alpha :as s]
            [expound.alpha :as expound]
            [devtools.core :as devtools]))

(set! s/*explain-out* expound/printer)

(enable-console-print!)

(devtools/install!)

#_(core/init!)
