(ns atom-cljfmt.core
  (:require
   ["atom" :as editor]
   [cljfmt.core :as cljfmt]))

;; ---
;; API

(def -notifications
  (.-notifications js/atom))

(def -workspace
  (.-workspace js/atom))


;; ---
;; UI


(defn notify-success [s]
  (.addSuccess -notifications s))


;; ---


(defn make-default-system []
  {:commands (.-commands js/atom)
   :subscriptions (editor/CompositeDisposable.)})

(defonce system-ref
  (atom (make-default-system)))


;; ---


(defn register-command! [system-ref command]
  (let [^js commands (:commands @system-ref)]
    (.add commands "atom-text-editor" (str "atom-cljfmt:" (-> command meta :name))
          #js {:didDispatch (deref command)})))

(defn register-subscription! [system-ref disposable]
  (let [^js subscriptions (:subscriptions @system-ref)]
    (.add subscriptions disposable)))

(defn dispose! [system-ref]
  (let [^js subscriptions (:subscriptions @system-ref)]
    (.dispose subscriptions)))


;; ---
;; COMMANDS


(defn format [^js event]
  (when-let [^js editor (.getActiveTextEditor -workspace)]
    (let [range    (.getSelectedBufferRange editor)
          position (.getCursorScreenPosition editor)
          text     (.getTextInBufferRange editor range)
          pretty   (cljfmt/reformat-string text)]
      (.setTextInBufferRange editor range pretty)
      (.setCursorScreenPosition editor position))))


;; ---


(defn setup-command! []
  (->> #'format
       (register-command! system-ref)
       (register-subscription! system-ref)))


;; ---
;; DEV


(defn ^:dev/before-load before []
  (js/console.log "Before" (clj->js  @system-ref))

  (dispose! system-ref)

  (reset! system-ref (make-default-system))

  (setup-command!))

(defn ^:dev/after-load after []
  (js/console.log "After" (clj->js @system-ref))

  (notify-success "^:dev/after-load"))


;; ---


(defn activate []
  (setup-command!)

  (js/console.log "Activated" (clj->js  @system-ref)))

(defn deactivate []
  (dispose! system-ref)

  (js/console.log "Deactivated" (clj->js  @system-ref)))
