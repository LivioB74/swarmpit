(ns swarmpit.router
  (:require [bidi.router :as br]
            [swarmpit.storage :as storage]
            [swarmpit.routes :as routes]
            [swarmpit.controller :as ctrl]
            [swarmpit.component.state :as state]
            [swarmpit.component.layout :as layout]))

(defonce location (atom nil))

(def location-domains
  {:index                 "Home"
   :service-list          "Services"
   :service-create-config "Services / Wizard"
   :service-create-image  "Services / Wizard"
   :service-info          "Services"
   :service-edit          "Services"
   :network-list          "Networks"
   :network-create        "Networks / Create"
   :network-info          "Networks"
   :node-list             "Nodes"
   :node-info             "Nodes"
   :task-list             "Tasks"
   :task-info             "Tasks"
   :user-list             "Users"
   :user-create           "Users / Create"
   :user-info             "Users"
   :registry-info         "Registries"
   :registry-list         "Registries"
   :registry-create       "Registries / Create"})

(def location-page
  #{:login nil})

;;; Router config

(defn- is-layout?
  "Check whether `loc` belong to layout. Not single page!"
  [loc]
  (not (contains? location-page (:handler loc))))

(defn- domain
  "Associate domain value based na handler"
  [loc]
  (get location-domains (:handler loc)))

(defn- route
  "Route to given `loc`"
  [loc]
  (ctrl/dispatch loc)
  (reset! location loc))

(defn- route-to-loc
  "Route to given `loc` and update state domain"
  [loc]
  (state/update-value [:domain] (domain loc) [:menu])
  (if (is-layout? loc)
    (layout/mount!))
  (route loc))

(defn- route-to-login
  "Route to login page"
  []
  (route {:handler :login}))

(defn- navigate
  [loc]
  (if (nil? (storage/get "token"))
    (route-to-login)
    (route-to-loc loc)))

(defn start
  []
  (let [router (br/start-router! routes/frontend {:on-navigate navigate})
        route (:handler @location)]
    (if (some? route)
      (br/set-location! router @location))))
