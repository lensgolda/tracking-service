(ns lens.tracking.interceptors)

(defn with-component
  "Injects component into request"
  [component]
  {:name ::with-component
   :enter (fn [ctx]
            (assoc-in ctx [:request :component] component))})