#!/usr/bin/env bb

(require 
  '[babashka.fs :as fs]
  '[clojure.string :as str]
  '[clojure.tools.cli :refer [parse-opts]])

(def parsed-params (parse-opts *command-line-args* {}))
(def script-base-dir (first (:arguments parsed-params)))

(if (nil? script-base-dir)
  (throw (Exception. "error: script base dir needs to be provided as first arg!")))

(def user (System/getenv "SUDO_USER"))
(if (nil? user)
  (throw (Exception. "error: script needs to be executed with sudo!")))

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
  (safe-sh-as-user "git" "clone" "https://github.com/syl20bnr/spacemacs" (str home "/.emacs.d"))
  (println "spacemacs set up"))

(defn- setup-android-scripts []
  (let [android-scripts-dir (str home "/Projects/android-scripts")]
    (-> (fs/file android-scripts-dir) (fs/delete-tree))
    (fs/create-dirs android-scripts-dir)
    (safe-sh "chown" "-R" (str user ":" user) android-scripts-dir)
    (safe-sh-as-user "git" "clone" "ssh://git@git.schro.fi:4242/schrofi/android-scripts.git" android-scripts-dir)))


(defn- link-config-files []
  (println "setting gtk theme..")
  (safe-sh-as-user "ln" "-fs" (str script-base-dir "/configs/config-files/gtk-config") "~/.gtkrc-2.0")
  (println "linking wallpaper..")
  (safe-sh-as-user "ln" "-fs" (str script-base-dir "/configs/config-files/wallpaper.jpg") "~/Pictures/wallpaper.jpg")
  (safe-sh-as-user "ln" "-fs" (str script-base-dir "/configs/config-files/bspwm-config") "~/.config/bspwm/bspwmrc")
  (safe-sh-as-user "ln" "-fs" (str script-base-dir "/configs/config-files/picom-config") "~/.config/picom/picom.conf")
  (safe-sh-as-user "ln" "-fs" (str script-base-dir "/configs/config-files/alacritty-config") "~/.config/alacritty/alacritty.toml")
  (safe-sh-as-user "ln" "-fs" (str script-base-dir "/configs/config-files/sxhkd-config") "~/.config/sxhkd/sxhkdrc"))

(defn- setup-system-services []
  (println "setting up system services..")
  ;;todo: user services
  (safe-sh "ln" "-s" "/etc/sv/bluetoothd" "/var/service")
  (safe-sh "usermod" "-a" "-G" "bluetooth" user))

(defn- install-fish-functions []
  ;;todo: symlink files in fish-functions to .config/fish/functions
  )

(println "applying configuration..")
(setup-spacemacs)

;;change shell to fish
(safe-sh "chsh" "-s" "/usr/bin/fish" user)
(println "changed shell to fish")

(println "setting up fish functions..")
(install-fish-functions)

(safe-sh-as-user "git" "config" "--global" "user.name" "Florian Schrofner")
(safe-sh-as-user "git" "config" "--global" "user.email" "florian@schro.fi")
(println "changed git user data")

;;creating projects dir
(let [projects-dir (str home "/Projects")]
  (fs/create-dirs projects-dir)
  (safe-sh "chown" (str user ":" user) "-R" projects-dir)
  (println "created project directory"))

(setup-android-scripts)
(println "set up android scripts")

(setup-system-services)
(println "set up system services")

(link-config-files)
(println "configuration applied")
