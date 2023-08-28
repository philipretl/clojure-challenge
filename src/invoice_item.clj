(ns invoice-item
  (:require [clojure.data.json :as json])
  (:require [clojure.string :as str])
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

(defn iva [item]
  (count (has-tax-rate-iva-equal-to-nineteen? (get item :taxable/taxes)))
  )
(defn ret [item]
  (count (has-retention-rate-equal-to-one? (get item :retentionable/retentions)))
  )

(defn only-iva-or-retention?
  [item]
  (if (= 1 (+ (iva item) (ret item)))
    true
    false
    )
  )

(defn filter-invoices
  [items]
  (vec (filter (fn [item]
                 (if (only-iva-or-retention? item) item)
                 )
               items
               )
       )

  )


;; Second Challenge
(defn retentions-mapper [retention-json]
  {:retentions/tax_category (:tax_category retention-json)
   :retentions/tax_rate     (:tax_rate retention-json)
   }
  )

(defn map-retentions [retentions, new_key]
  (map (fn [retention]
         (retentions-mapper retention)
         )
       retentions
       )
  )

(defn invoice-taxes-mapper [invoice-taxes-json]
  {:tax/category (keyword (str/lower-case (:tax_category invoice-taxes-json)))
   :tax/rate     (double (:tax_rate invoice-taxes-json))
   }
  )

(defn map-item-tax [taxes]
  (map (fn [tax]
         (invoice-taxes-mapper tax)
         )
       taxes
       )
  )

(defn items-mapper [items-json-map]
  {:invoice-item/price    (:price items-json-map)
   :invoice-item/quantity (:quantity items-json-map)
   :invoice-item/sku      (:sku items-json-map)
   :invoice-item/taxes    (vec (map-item-tax (:taxes items-json-map)))
   }
  )

(defn map-items [items]
  (map (fn [item]
         (items-mapper item)
         )
       items
       )
  )

(defn invoice-mapper [json-map]
  {:invoice/issue-date         (Date.)
   :invoice/order_reference    (:order_reference json-map)
   :invoice/payment_date       (:payment_date json-map)
   :invoice/payment_means      (:payment_means json-map)
   :invoice/payment_means_type (:payment_means_type json-map)
   :invoice/number             (:number json-map)
   :invoice/items              (vec (map-items (:items json-map)))
   :invoice/customer           {
                                :customer/name  (get-in json-map [:customer :company_name])
                                :customer/email (get-in json-map [:customer :email])
                                }
   :invoice/retentions         (vec (map-retentions (:retentions json-map) (name :retentions)))
   })

(defn load-data-from-json-file [invoice_json_path]
  (get (json/read-str invoice_json_path :key-fn keyword) :invoice)
  )


(defn invoice
  [invoice]
  (invoice-mapper (load-data-from-json-file invoice))
  )