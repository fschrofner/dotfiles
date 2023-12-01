#!/usr/bin/env bb

(require 
  '[babashka.fs :as fs]
  '[clojure.tools.cli :refer [parse-opts]]
  '[clojure.string :as string])

;;todo: import from base.clj
(defn- safe-sh [& commands]
  (as-> (apply shell/sh commands) $
    (if (= (:exit $) 0) $ (throw (Exception. (:err $))))))

(def cli-options
  [["-u" "--user USER" "The user to set up"]])

(def options (:options (parse-opts *command-line-args* cli-options)))

(if-let [user (:user options)]
  (let [home (str "/home/" user)]
    ;;todo: fetch spacemacs
    ;;todo: disable locking when closing lid on ac
    (println "applying configuration..")

    ;; installing spacemacs
    (-> (fs/file (str home "/.emacs.d")) (fs/delete-tree))
    (safe-sh "git" "clone" "https://github.com/syl20bnr/spacemacs" (str home "/.emacs.d"))
    (safe-sh "chown" (str user ":" user) "-R" (str home "/.emacs.d"))

    (println "configuration applied"))
  (println "error: must provide a user to set up"))