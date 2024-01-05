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
    (fs/create-dirs android-scripts-dir)
    (safe-sh "chown" "-R" (str user ":" user) android-scripts-dir)
    (safe-sh-as-user "git" "clone" "ssh://git@git.schro.fi:4242/schrofi/android-scripts.git" android-scripts-dir)))

(defn- setup-i3 []
  (println "setting up i3..")
  (println "setting i3 as default wm..")
  (safe-sh-as-user "gsettings" "set" "org.mate.session.required-components" "windowmanager" "i3")
  (safe-sh-as-user "gsettings" "set" "org.mate.session" "required-components-list" "['windowmanager', 'panel', 'dock']"))

(defn- setup-theme []
  (println "setting up fonts..")
  (safe-sh-as-user "gsettings" "set" "org.mate.interface" "font-name" "Fira Code Regular 10.0")
  (safe-sh-as-user "gsettings" "set" "org.mate.interface" "document-font-name" "Fira Code Regular 10.0")
  (safe-sh-as-user "gsettings" "set" "org.mate.interface" "monospace-font-name" "Fira Code Regular 10.0")
  (safe-sh-as-user "gsettings" "set" "org.mate.Marco.general" "titlebar-font" "Fira Code Bold 10.0")
  (safe-sh-as-user "gsettings" "set" "org.mate.caja.desktop" "font" "Fira Code Regular 10.0")
  (safe-sh-as-user "ln" "-fs" (str script-base-dir "/configs/config-files/gtk-config") "~/.gtkrc-2.0")
  (safe-sh-as-user "mkdir" "-p" "~/.config/i3")
  (safe-sh-as-user "ln" "-fs" (str script-base-dir "/configs/config-files/i3-config") "~/.config/i3/config")
  (println "linking wallpaper..")
  (safe-sh-as-user "ln" "-fs" (str script-base-dir "/configs/config-files/wallpaper.jpg") "~/Pictures/wallpaper.jpg")
  (safe-sh-as-user "gsettings" "set" "org.mate.background" "picture-filename" "~/Pictures/wallpaper.jpg"))

(defn- setup-system-services []
  (println "setting up system services..")
  ;;todo: user services
  (safe-sh "ln" "-s" "/etc/sv/bluetoothd" "/var/service")
  (safe-sh "usermod" "-a" "-G" "bluetooth" user))

;;todo: disable locking when closing lid on ac
(println "applying configuration..")

(setup-spacemacs)

;;change shell to fish
(safe-sh "chsh" "-s" "/usr/bin/fish" user)
(println "changed shell to fish")

(safe-sh-as-user "git" "config" "--global" "user.name" "Florian Schrofner")
(safe-sh-as-user "git" "config" "--global" "user.email" "florian@schro.fi")
(println "changed git user data")

(setup-i3)
(println "set up i3")
(setup-theme)

;;creating projects dir
(let [projects-dir (str home "/Projects")]
  (fs/create-dirs projects-dir)
  (safe-sh "chown" (str user ":" user) "-R" projects-dir)
  (println "created project directory"))

(setup-android-scripts)
(println "set up android scripts")

(setup-system-services)
(println "set up system services")


(println "configuration applied")
