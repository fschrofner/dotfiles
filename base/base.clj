#!/usr/bin/env bb

(require 
 '[clojure.string :as str])

;;executes shell command but throws exception on error
(defn- safe-sh [& commands]
  (as-> (apply shell/sh commands) $
    (if (= (:exit $) 0) $ (throw (Exception. (:err $))))))

;;executes command as user calling this script with sudo
(defn- safe-sh-as-user [& commands]
  (safe-sh "su" "-c" (str/join " " (map #(if (str/includes? % " ") (str "\"" % "\"") %) commands)) user))
