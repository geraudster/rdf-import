(ns rdf-import.core)

(use 'incanter.core
  'edu.ucdenver.ccp.kr.kb
  'edu.ucdenver.ccp.kr.rdf
  'edu.ucdenver.ccp.kr.sparql
  'edu.ucdenver.ccp.kr.sesame.kb
  'clojure.set)
(import [java.io File])

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
        ("dcam" "http://purl.org/dc/dcam/"))))

(def tstore (init-kb (kb-memstore)))


(defn load-data
  [k rdf-file q]
  (load-rdf-file k rdf-file))

(def q '((?/b dcterms/title ?/title )))

(def rdf-file-location "/home/geraud/projets/cache/epub/11511/pg11511.rdf")
(load-data tstore (File. rdf-file-location) q)