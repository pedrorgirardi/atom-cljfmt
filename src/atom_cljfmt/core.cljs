(ns atom-cljfmt.core
  (:require
   ["atom" :as editor]
   [cljfmt.core :as cljfmt]))

;; ---


(def -notifications
  (.-notifications js/atom))

(def -workspace
  (.-workspace js/atom))

(def -commands
  (.-commands js/atom))

(def subscriptions
  (editor/CompositeDisposable.))


;; ---


(defn notify-success [s]
  (.addSuccess -notifications s))


(defn register-text-editor-command! [^js registry command]
  (let [s (str "atom-cljfmt:" (-> command meta :name))
        f (deref command)]
    (.add registry "atom-text-editor" s f)))


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


(defn init! []
  (let [disposable (register-text-editor-command! -commands #'format)]
    (.add subscriptions disposable)))


;; ---
;; DEV


(defn ^:dev/before-load before []
  (js/console.log "Dispose..." subscriptions)

  (.dispose subscriptions)

  (js/console.log "Disposed" subscriptions))

(defn ^:dev/after-load after []
  (init!)

  (js/console.log "Reloaded" subscriptions)

  (notify-success "Reloaded"))


;; ---


(defn activate []
  (init!)

  (js/console.log "Activated" subscriptions))

(defn deactivate []
  (.dispose subscriptions)

  (js/console.log "Deactivated" subscriptions))
