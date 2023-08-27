(ns invoice-item
  (:require [clojure.data.json :as json])
  (:import (java.util Date)))


(defn- discount-factor [{:keys [discount-rate]
                         :or   {discount-rate 0}}]
  (- 1 (/ discount-rate 100.0)))

(defn subtotal
  [{:keys [precise-quantity precise-price discount-rate]
    :as   item
    :or   {discount-rate 0}}]
  (if (or (< precise-quantity 0) (< precise-price 0) (< discount-rate 0))
    0.0
    (* precise-price precise-quantity (discount-factor item)))
  )


;; First Challenge

(defn has-retention-rate-equal-to-one?
  [retentions]
  (filter (fn [retention_item] (if (= 1 (get retention_item :retention/rate))
                                 retention_item
                                 )
            )
          retentions
          )
  )

(defn has-tax-rate-iva-equal-to-nineteen?
  [taxes]
  (filter (fn [tax] (if (= 19 (get tax :tax/rate))
                      tax
                      )
            )
          taxes
          )
  )


(defn only-iva-or-retention?
  [item]
  (
    if (= 2 (+ (count (has-tax-rate-iva-equal-to-nineteen? (get item :taxable/taxes))) (count (has-retention-rate-equal-to-one? (get item :retentionable/retentions))))
          )
    false
    true
    )
  )

(defn filter-invoices
  [items]
  (filter (fn [item]
            (if (only-iva-or-retention? item) item)
            )
          items
          )

  )


;; Second Challenge

(def array_values {:taxes [
                           {
                            :tax_category "IVA"
                            :tax_rate     19
                            }
                           ]
                   }
  )


(defn replace-if-is-array [value, new_key]
  (vec (map (fn [item]
              (reduce (fn [row-map [key value]]
                        (assoc row-map (#(str new_key "/" %) (name key)) value)
                        )
                      {}
                      item)
              )
            value

            )
       )
  )

(defn map-items [items, new_key]
  (replace-if-is-array items new_key)
  )

(defn map-retentions [retentions, new_key]
  (replace-if-is-array retentions new_key)
  )

(defn change-keys [old_map new_key]
  (reduce-kv
    (fn [new-map key value]
      ;;(println key)
      (assoc new-map (new_key (name key))
                     (cond
                       (= key :items) (map-items value key)
                       (= key :issue_date) (Date.)
                       (= key :retentions) (map-retentions value key)
                       :else value
                       )
                     )
      )
    {}
    old_map
    )
  )

(def map_values {:customer {:company_name "ANDRADE RODRIGUEZ MANUEL ALEJANDRO", :email "cgallegoaecu@gmail.com"}})


(defn replace-if-is-map [value, new_key]
  (reduce (fn [row-map [key value]]
            (assoc row-map (#(str new_key "/" %) (name key)) value)
            )
          {}
          (get value (first (keys value)))
          )
  )

(defn load-json-file
  [invoice]
  (json/read-str invoice :key-fn keyword)
  )




(defn invoice
  [invoice]
  (
    ;;load-json-file invoice
    ;;get (load-json-file invoice) :invoice
    change-keys (get (load-json-file invoice) :invoice) #(str ":invoice/" %)
                ;;map-array values #(str "invoice/" %)
                ;;values
                ;;replace-if-is-map map_values (first (keys map_values))
                ;;replace-if-is-array array_values (first (keys array_values))
                ;;(prn (get array_values (first(keys array_values))))

                )
  )