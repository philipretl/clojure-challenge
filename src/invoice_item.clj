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


(defn transform-key [new-key value]
  (read-string (str (keyword new-key) "/" value))
  )



(defn replace-if-is-array [value, new_key]
  (vec (map (fn [item]
              (reduce (fn [row-map [key value]]
                        (assoc row-map (transform-key new_key
                                                      (cond
                                                        (= key :tax_category) "category"
                                                        (= key :tax_rate) "rate"
                                                        ;;(= key :taxes) "tax"
                                                        :else (name key)
                                                        )
                                                      )
                                       (cond
                                         (= key :taxes) (replace-if-is-array value "tax")
                                         :else value
                                         )
                                       )
                        )
                      {}
                      item)
              )
            value

            )
       )
  )

(defn map-items [items, new_key]
  (replace-if-is-array items "invoice-item")
  )

(defn map-retentions [retentions, new_key]
  (replace-if-is-array retentions new_key)
  )

(defn map-customer [value, new_key]
  (reduce (fn [row-map [key value]]
            (assoc row-map (transform-key new_key (cond
                                                    (= key :company_name) "name"
                                                    :else (name key)
                                                    )
                                          ) value)
            )
          {}
          value
          )
  )

(defn change-keys [old_map new_key]
  (reduce-kv
    (fn [new-map key value]
      (assoc new-map (transform-key new_key (name key))
                     (cond
                       (= key :issue_date) (Date.)
                       ;;(= key :items) (map-items value (name key))
                       ;;(= key :customer) (map-customer value (name key))
                       (= key :items) {}
                       ;;(= key :retentions) (map-retentions value (name key))
                       (= key :retentions) {}
                       :else {}
                       )
                     )
      )
    {}
    old_map
    )
  )

(def map_values {:customer {:company_name "ANDRADE RODRIGUEZ MANUEL ALEJANDRO", :email "cgallegoaecu@gmail.com"}})


(defn load-json-file
  [invoice]
  (json/read-str invoice :key-fn keyword)
  )



(defn invoice-mapper [json-map]
  {:invoice/issue-date         (Date.)
   :invoice/order_reference    (:order_reference json-map)
   :invoice/payment_date       (:payment_date json-map)
   :invoice/payment_means      (:payment_means json-map)
   :invoice/payment_means_type (:payment_means_type json-map)
   :invoice/number             (:number json-map)
   :invoice/items              (map-items (:items json-map) (name :items))
   :invoice/customer           {
                                :customer/name  (get-in json-map [:customer :company_name])
                                :customer/email (get-in json-map [:customer :email])
                                }
   :invoice/retentions         (map-retentions (:retentions json-map) (name :retentions))
   })

(defn load-data-from-json-file [invoice_json_path]
  (get (json/read-str invoice_json_path :key-fn keyword) :invoice)
  )


(defn invoice
  [invoice]
  (
    invoice-mapper (load-data-from-json-file invoice)
                   ;;load-json-file invoice
                   ;;get (load-json-file invoice) :invoice
                   ;;change-keys (get (load-json-file invoice) :invoice) "invoice"
                   ;;map-array values #(str "invoice/" %)
                   ;;values
                   ;;replace-if-is-map map_values (first (keys map_values))
                   ;;replace-if-is-array array_values (first (keys array_values))
                   ;;(prn (get array_values (first(keys array_values))))

                   )
  )