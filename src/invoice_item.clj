(ns invoice-item
  (:require [clojure.data.json :as json])
  )


(defn- discount-factor [{:invoice-item/keys [discount-rate]
                         :or                {discount-rate 0}}]
  (- 1 (/ discount-rate 100.0)))

(defn subtotal
  [{:invoice-item/keys [precise-quantity precise-price discount-rate]
    :as                item
    :or                {discount-rate 0}}]
  (* precise-price precise-quantity (discount-factor item)))


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


(defn invoice
  [invoice]
  (
    json/read-str invoice :key-fn keyword
                  )
  )