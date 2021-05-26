package com.sod.pricing;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
 
public class Util {

    public static File getFileFromResource(String fileName, ClassLoader classLoader) throws URISyntaxException{

        URL resource = classLoader.getResource(fileName);
        if (resource == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return new File(resource.toURI());
        }

    }
     
	public static double getNullableDoubleDbField(ResultSet rs, Schema.FIELDS fieldName) throws SQLException {
		if(rs.getObject(fieldName.name()) == null)
			 return Double.NaN;
		else return rs.getDouble(fieldName.name());
	}
    
}
