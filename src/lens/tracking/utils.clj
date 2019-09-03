(ns lens.tracking.utils)

(defn response
  ([code]
   (response code nil))
  ([code body]
   {:status code
    :body body}))
