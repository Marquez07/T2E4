package appagenda;

import entidades.Provincia;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class ConsultaProvincias {

    public static void main(String[] args) {
        Map<String,String> emfProperties = new HashMap<String,String>();
        //emfProperties.put("javax.persistence.jdbc.user", "APP");
        //emfProperties.put("javax.persistence.jdbc.password", "App");
        emfProperties.put("javax.persistence.schema-generation.database.action","create");
        EntityManagerFactory emf= Persistence.createEntityManagerFactory("AppAgendaPU",emfProperties);
        EntityManager em = emf.createEntityManager();
        Query queryProvincias = em.createNamedQuery("Provincia.findAll");
        List<Provincia> listProvincias = queryProvincias.getResultList();
    
        for(Provincia provincia : listProvincias)
        {
            System.out.println(provincia.getNombre()); 
        }
        
        System.out.println();
        
        Query queryProvinciaUnica = em.createNamedQuery("Provincia.findByNombre"); 
        queryProvinciaUnica.setParameter("nombre", "ALBACETE"); 
        List<Provincia> listProvinciasUnicas = queryProvinciaUnica.getResultList();
        
        for(Provincia provincia:listProvinciasUnicas)
        {
            System.out.println(provincia.getId()+":"+provincia.getNombre());
        }
        
        Provincia provinciaId4=em.find(Provincia.class,4);
        
        if (provinciaId4 != null)
        { 
            System.out.print(provinciaId4.getId() + ":");
            System.out.println(provinciaId4.getNombre());
        } 
        else 
        { 
            System.out.println("No hay ninguna provincia con ID=4");
        }
        
        em.getTransaction().begin();
        for(Provincia provincia : listProvinciasUnicas)
        { 
            provincia.setCodigo("AB"); 
            em.merge(provincia); 
        }
        
        em.getTransaction().commit();
        
        Provincia provinciaId15 = em.find(Provincia.class, 15);
        em.getTransaction().begin();
        
        if (provinciaId15 != null)
        { 
            em.remove(provinciaId15);
        }
        else
        { 
            System.out.println("No hay ninguna provincia con ID=15");
        } 
        em.getTransaction().commit();
        
        em.close();
        emf.close();
        try{ 
            DriverManager.getConnection("jdbc:derby:BDAgenda;shutdown=true"); 
        } catch (SQLException ex){
            
        }
    }
    
}
