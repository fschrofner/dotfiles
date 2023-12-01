#!/usr/bin/env bb

(require 
  '[babashka.fs :as fs]
  '[clojure.string :as str])

(def user (System/getenv "SUDO_USER"))

;;todo: import from base.clj
(defn- safe-sh [& commands]
  (as-> (apply shell/sh commands) $
    (if (= (:exit $) 0) $ (throw (Exception. (:err $))))))

(defn- safe-sh-as-user [& commands]
  (safe-sh "su" "-c" (str/join " " (map #(if (str/includes? % " ") (str "\"" % "\"") %) commands)) user))

(defn- setup-spacemacs [user home]
  ;; installing spacemacs
  (println "setting up spacemacs..")
  (-> (fs/file (str home "/.emacs.d")) (fs/delete-tree))
  (safe-sh "git" "clone" "https://github.com/syl20bnr/spacemacs" (str home "/.emacs.d"))
  (safe-sh "chown" (str user ":" user) "-R" (str home "/.emacs.d"))
  (println "spacemacs set up"))

(defn- setup-android-scripts [user home]
  (let [android-scripts-dir (str home "/Projects/android-scripts")]
    (fs/create-dirs android-scripts-dir)
    (safe-sh "git" "clone" "ssh://git@git.schro.fi:4242/schrofi/android-scripts.git" android-scripts-dir)
    (safe-sh "chown" (str user ":" user) "-R" android-scripts-dir)))

(let [home (str "/home/" user)]
  ;;todo: fetch spacemacs
  ;;todo: disable locking when closing lid on ac
  (println "applying configuration..")

  ;;(setup-spacemacs user home)

  ;;change shell to fish
  (safe-sh "chsh" "-s" "/usr/bin/fish" user)
  (println "changed shell to fish")

  (safe-sh-as-user "git" "config" "--global" "user.name" "Florian Schrofner")
  (safe-sh-as-user "git" "config" "--global" "user.email" "florian@schro.fi")
  (println "changed git user data")

  ;;creating projects dir
  (let [projects-dir (str home "/Projects")]
    (fs/create-dirs projects-dir)
    (safe-sh "chown" (str user ":" user) "-R" projects-dir)
    (println "created project directory"))

  (setup-android-scripts user home)
  (println "set up android scripts")

  (println "configuration applied"))
