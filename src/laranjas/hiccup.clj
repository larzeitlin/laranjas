(ns laranjas.hiccup
  (:require [hiccup2.core :as h]))

(defmacro html-str [v]
  (-> v h/html str))
