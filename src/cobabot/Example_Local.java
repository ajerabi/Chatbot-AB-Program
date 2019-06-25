// Copyright (c) 2011. This source code is available under the terms of the GNU Lesser General Public License (LGPL)
// Author: Mario Volke <volke@derivo.de>
// derivo GmbH, James-Franck-Ring, 89081 Ulm 

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


/**
 * @author Farrel
 * @author Fizikri
 */
public class Example_Local
{
    private static QueryEngine engine;

    /**
     * @param stringAIMLResult
     */
    public static void querySPARQL_DL(String stringAIMLResult)
    {
//        Logger.getRootLogger().setLevel(Level.OFF);

        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

            OWLOntology ont = manager.loadOntologyFromOntologyDocument(new File("C:\\Users\\ASUS X450J\\Documents\\Ontology Practices\\Persiapan Seminar 3\\system-unit-of-desktop-computer-inferred.owl"));

            StructuralReasonerFactory factory = new StructuralReasonerFactory();
            OWLReasoner reasoner = factory.createReasoner(ont);
            reasoner.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.OBJECT_PROPERTY_ASSERTIONS);
            engine = QueryEngine.create(manager, reasoner, true);

            processQuery(StringParserForSPARQL_DL.function(stringAIMLResult).toString());
        }
        catch(UnsupportedOperationException exception) {
            System.out.println("Unsupported reasoner operation.");
        }
        catch(OWLOntologyCreationException e) {
            System.out.println("Could not load the wine ontology: " + e.getMessage());
        }
    }

    private static void processQuery(String q)
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