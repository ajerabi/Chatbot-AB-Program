package cobabot;

import de.derivo.sparqldlapi.Query;
import de.derivo.sparqldlapi.QueryEngine;
import de.derivo.sparqldlapi.QueryResult;
import de.derivo.sparqldlapi.exceptions.QueryEngineException;
import de.derivo.sparqldlapi.exceptions.QueryParserException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.io.File;

public class ExampleSPARQLDL {
    private static QueryEngine engine;

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        try {
            // Create an ontology manager
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            // Load the wine ontology from the web.
            /*OWLOntology ont = manager.loadOntologyFromOntologyDocument(IRI.create("http://www.w3.org/TR/owl-guide/wine.rdf"));*/
            OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("C:\\Users\\Delvin V\\Documents\\TA\\Chatbot-AB-Program\\owl\\system-unit-of-desktop-computer-inferred.owl"));

            // Create an instance of an OWL API reasoner (we use the OWL API built-in StructuralReasoner for the purpose of demonstration here)
            StructuralReasonerFactory factory = new StructuralReasonerFactory();
            OWLReasoner reasoner = factory.createReasoner(ont);
            // Optionally let the reasoner compute the most relevant inferences in advance
            reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS,InferenceType.OBJECT_PROPERTY_ASSERTIONS);

            // Create an instance of the SPARQL-DL query engine
            engine = QueryEngine.create(manager, reasoner, true);

            // Some queries which cover important basic language constructs of SPARQL-DL

            // All white wines (all individuals of the class WhiteWine and sub classes thereof)

			/*
			//Get all classes
			processQuery(
					"PREFIX myOnto: <http://www.semanticweb.org/asusx450j/ontologies/2019/2/system-unit-of-desktop-computer#>\n" +
							"SELECT ?c WHERE {\n" +
								"Class(?c)" +
							"}"
			);
			 */

			/*
			//Get all individuals from one specific class
			processQuery(
					"PREFIX myOnto: <http://www.semanticweb.org/asusx450j/ontologies/2019/2/system-unit-of-desktop-computer#>\n" +
							"SELECT ?Component WHERE {\n" +
							"Type(?Component, myOnto:HardDiskDrive) \n" +
							"}"
			);
			 */

			/*
			//Get all components with its features
			processQuery(
					"PREFIX myOnto: <http://www.semanticweb.org/asusx450j/ontologies/2019/2/system-unit-of-desktop-computer#>\n" +
							"SELECT ?Component ?Feature WHERE {\n" +
								"PropertyValue(?Component, myOnto:hasFeature, ?Feature)" +
							"}"
			);
			 */

			/*
			//Get individual from one specific class and specific feature
			processQuery(
					"PREFIX myOnto: <http://www.semanticweb.org/asusx450j/ontologies/2019/2/system-unit-of-desktop-computer#>\n" +
							"SELECT ?Component WHERE {\n" +
								"Type(?Component, myOnto:HardDiskDrive), \n" +
								"PropertyValue(?Component, myOnto:hasFeature, myOnto:LowNoise)" +
							"}"
			);

			 */

			/*
			//Get individual from one specific class and specific performance classification
			processQuery(
					"PREFIX myOnto: <http://www.semanticweb.org/asusx450j/ontologies/2019/2/system-unit-of-desktop-computer#>\n" +
							"SELECT ?Component WHERE {\n" +
								"Type(?Component, myOnto:CPU-Intel), \n" +
								"PropertyValue(?Component, myOnto:hasPerformanceClassification, myOnto:Enthusiast)" +
							"}"
			);

			 */

            //Simulation 1 - Office
            processQuery(
                    "PREFIX myOnto: <http://www.semanticweb.org/asusx450j/ontologies/2019/2/system-unit-of-desktop-computer#>\n" +
                            "SELECT ?Component WHERE {\n" +
                            "Type(?Component, myOnto:HardDiskDrive), \n" +
                            "PropertyValue(?Component, myOnto:hasFeature, myOnto:LowNoise)" +
                            "}" +
                            "OR WHERE {\n" +
                            "Type(?Component, myOnto:CPU-Intel), \n" +
                            "PropertyValue(?Component, myOnto:hasPerformanceClassification, myOnto:Enthusiast)" +
                            "}"
            );
        }
        catch(UnsupportedOperationException exception) {
            System.out.println("Unsupported reasoner operation.");
        }
        catch(OWLOntologyCreationException e) {
            System.out.println("Could not load the wine ontology: " + e.getMessage());
        }
    }

    public static void processQuery(String q)
    {
        try {
            long startTime = System.currentTimeMillis();

            // Create a query object from it's string representation
            Query query = Query.create(q);

            System.out.println("Excecute the query:");
            System.out.println(q);
            System.out.println("-------------------------------------------------");

            // Execute the query and generate the result set
            QueryResult result = engine.execute(query);

            if(query.isAsk()) {
                System.out.print("Result: ");
                if(result.ask()) {
                    System.out.println("yes");
                }
                else {
                    System.out.println("no");
                }
            }
            else {
                if(!result.ask()) {
                    System.out.println("Query has no solution.\n");
                }
                else {
                    System.out.println("Results:");
                    System.out.print(result);
                    System.out.println("-------------------------------------------------");
                    System.out.println("Size of result set: " + result.size());
                }
            }

            System.out.println("-------------------------------------------------");
            System.out.println("Finished in " + (System.currentTimeMillis() - startTime) / 1000.0 + "s\n");
        }
        catch(QueryParserException e) {
            System.out.println("Query parser error: " + e);
        }
        catch(QueryEngineException e) {
            System.out.println("Query engine error: " + e);
        }
    }


}
