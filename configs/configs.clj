#!/usr/bin/env bb

(require 
  '[babashka.fs :as fs]
  '[clojure.string :as str])

(def user (System/getenv "SUDO_USER"))
(def home (str "/home/" user))

;;todo: import from base.clj
(defn- safe-sh [& commands]
  (as-> (apply shell/sh commands) $
    (if (= (:exit $) 0) $ (throw (Exception. (:err $))))))

(defn- safe-sh-as-user [& commands]
  (safe-sh "su" "-c" (str/join " " (map #(if (str/includes? % " ") (str "\"" % "\"") %) commands)) user))

(defn- setup-spacemacs []
  ;; installing spacemacs
  (println "setting up spacemacs..")
  (-> (fs/file (str home "/.emacs.d")) (fs/delete-tree))
  (safe-sh "git" "clone" "https://github.com/syl20bnr/spacemacs" (str home "/.emacs.d"))
  (safe-sh "chown" (str user ":" user) "-R" (str home "/.emacs.d"))
  (println "spacemacs set up"))

(defn- setup-android-scripts []
  (let [android-scripts-dir (str home "/Projects/android-scripts")]
    (fs/create-dirs android-scripts-dir)
    (safe-sh "git" "clone" "ssh://git@git.schro.fi:4242/schrofi/android-scripts.git" android-scripts-dir)
    (safe-sh "chown" (str user ":" user) "-R" android-scripts-dir)))

(defn- setup-i3 []
  (println "setting up i3..")
  (println "clearing xfce keyboard bindings..")
  (as-> (safe-sh "xfconf-query" "--channel" "xfce4-keyboard-shortcuts" "--list") $
    (:out $)
    (str/split-lines $)
    (doseq [property $]
      ;;we actually need to ignore errors here, as we will hit a few undefined properties
      (shell/sh "xfconf-query" "--channel" "xfce4-keyboard-shortcuts" "--property" property "--set" ""))))

;;todo: fetch spacemacs
;;todo: disable locking when closing lid on ac
(println "applying configuration..")

;;(setup-spacemacs)

;;change shell to fish
(safe-sh "chsh" "-s" "/usr/bin/fish" user)
(println "changed shell to fish")

(safe-sh-as-user "git" "config" "--global" "user.name" "Florian Schrofner")
(safe-sh-as-user "git" "config" "--global" "user.email" "florian@schro.fi")
(println "changed git user data")

(setup-i3)
(println "set up i3")

;;creating projects dir
(let [projects-dir (str home "/Projects")]
  (fs/create-dirs projects-dir)
  (safe-sh "chown" (str user ":" user) "-R" projects-dir)
  (println "created project directory"))

(setup-android-scripts)
(println "set up android scripts")

(println "configuration applied")
