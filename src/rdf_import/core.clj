(ns rdf-import.core
  (:import [java.io File])
  (:require [clojure.pprint]))

(use 'incanter.core
  'edu.ucdenver.ccp.kr.kb
  'edu.ucdenver.ccp.kr.rdf
  'edu.ucdenver.ccp.kr.sparql
  'edu.ucdenver.ccp.kr.sesame.kb
  'clojure.set
  '[clojure.java.io :only (file)]
  'clojure.pprint)

(defn kb-memstore
  "This creates a Sesame triple store in memory."
  []
  (kb :sesame-mem))

(defn init-kb
  "This creates an in-memory knowledge base and
  initializes it with a default set of namespaces."
  [kb-store]
  (register-namespaces
    kb-store
    '(("owl" "http://www.w3.org/2002/07/owl#")
       ("xsd" "http://www.w3.org/2001/XMLSchema#")
       ("rdf" "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
        ("xsd" "http://www.w3.org/2001/XMLSchema#")
        ("dc" "http://purl.org/dc/elements/1.1/")
        ("dcterms" "http://purl.org/dc/terms/")
        ("dcam" "http://purl.org/dc/dcam/")
       ("pgterms" "http://www.gutenberg.org/2009/pgterms/"))))

(def tstore (init-kb (kb-memstore)))


(defn load-data
  [k rdf-file]
  (load-rdf-file k rdf-file))

(def q '((?/b dcterms/title ?/title )
          (?/b dcterms/creator ?/creator)
          (?/creator pgterms/alias ?/agent)
          (:union ((?/format dcterms/isFormatOf ?/b )))))

;;;(def rdf-file-location "/home/geraud/projets/cache/epub/11511/pg11511.rdf")
;;;(load-data tstore (File. rdf-file-location) q)

(def rdf-location "/home/geraud/projets/cache/epub")
(def rdf-file-seq (filter (fn [f]
                            (-> (.getName f)
                              (.endsWith ".rdf")))
                    (file-seq (file rdf-location))))
(first rdf-file-seq)
(clojure.pprint/pprint (map (fn [^File f]
       (load-data tstore f)
       f)
  (take 10 rdf-file-seq)))

(clojure.pprint/pprint (query tstore q))
(to-dataset (map (fn [b]
       (into {} (map (fn[[k v]]
                [(keyword (name k)) (clojure.string/replace v #"\n" " ")]) b)))
  (query tstore q)))

(defn aggregate-book [acc tuple ]
    (let [{key :b} tuple
          key-keyword (keyword key)]
      (->> {key-keyword tuple}
        (merge-with (fn [res latter]
                      (merge res {:format (append-format (:format latter) (:format res))} ))
          acc ))))

(defn append-format [f formats]
  (flatten (conj (list f) formats)))

(def result (map (fn [b]
                   (into {} (map (fn[[k v]]
                                   [(keyword (name k)) (clojure.string/replace v #"\n" " ")]) b)))
              (query tstore q)))

(def aggregated-results (reduce aggregate-book {} result))
(clojure.pprint/pprint aggregated-results)
(to-dataset (vals aggregated-results))
