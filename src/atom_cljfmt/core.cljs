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

(defn active-text-editor []
  (.getActiveTextEditor -workspace))

(defn buffer-range [^js editor]
  (-> editor
      (.getBuffer)
      (.getRange)))

(defn current-scope [^js editor]
  (-> editor
      (.getGrammar)
      (.-scopeName)))


;; ---
;; UI


(defn success-notification [s]
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
  (let [^js editor (active-text-editor)
        ^js range  (some-> editor (buffer-range))]
    (when range
      (let [position (.getCursorScreenPosition editor)
            text     (.getTextInBufferRange editor range)
            text     (cljfmt/reformat-string text)]
        (.setTextInBufferRange editor range text)
        (.setCursorScreenPosition editor position)))))


;; ---


(defn register! []
  (->> #'format
       (register-command! system-ref)
       (register-subscription! system-ref)))

(defn ^:dev/before-load before []
  (js/console.log "Before" (clj->js  @system-ref))

  (dispose! system-ref)

  (reset! system-ref (make-default-system))

  (register!))

(defn ^:dev/after-load after []
  (js/console.log "After" (clj->js @system-ref))

  (success-notification "^:dev/after-load"))

(defn activate []
  (register!)

  (js/console.log "Activated" (clj->js  @system-ref)))

(defn deactivate []
  (dispose! system-ref)

  (js/console.log "Deactivated" (clj->js  @system-ref)))
