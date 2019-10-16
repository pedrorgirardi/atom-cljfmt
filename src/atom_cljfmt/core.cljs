(ns atom-cljfmt.core
  (:require
   ["atom" :as atom-env]
   [cljfmt.core :as cljfmt]))

(def -notifications
  (.-notifications js/atom))

(def -workspace
  (.-workspace js/atom))

(def -commands
  (.-commands js/atom))

(def subscriptions
  (atom-env/CompositeDisposable.))


;; ---


(defn format [^js event]
  (when-let [^js editor (.getActiveTextEditor -workspace)]
    (let [range    (.getSelectedBufferRange editor)
          position (.getCursorScreenPosition editor)
          text     (.getTextInBufferRange editor range)
          pretty   (cljfmt/reformat-string text)]
      (.setTextInBufferRange editor range pretty)
      (.setCursorScreenPosition editor position))))

(defn init []
  (let [disposable (.add -commands "atom-text-editor" "atom-cljfmt:format"
                         #js {:displayName "cljfmt: Format Selection"
                              :didDispatch format})]
    (.add subscriptions disposable)))


;; ---
;; DEV


(defn ^:dev/before-load before []
  (js/console.log "Will dispose..." subscriptions)

  (.dispose subscriptions)

  (js/console.log "Disposed" subscriptions))

(defn ^:dev/after-load after []
  (init)

  (js/console.log "Reloaded" subscriptions)

  (.addSuccess -notifications "Reloaded"))


;; ---
;; EXPORTS


(defn activate []
  (init))

(defn deactivate []
  (.dispose subscriptions))
