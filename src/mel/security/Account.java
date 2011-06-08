/*
 * Copyright (c) 2009  Mel Nicholson.
 * All Rights Reserved.
 */

package mel.security;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author charidan
 */
public class Account
{
    private String name;
    private String passwd;
    private Set<String> auth = new HashSet<String>();

    public Account(String name, String passwd)
    {
        this.name = name;
        this.passwd = passwd;
    }

    public boolean isAuthorized(String feature)
    {
        if(feature == null) return true;
        return auth.contains(feature);
    }

    /**
     * only AccountManager should use this -- it is responsible to save
     */
    void addAuthorization(String feature)
    {
        auth.add(feature);
    }
    
    public Set<String> getAuth()
    {
        return auth;
    }

    public String getPassword()
    {
        return passwd;
    }

    public void authenticate(String password) throws SecurityException
    {
        if(!passwd.equals(password)) throw new SecurityException("Incorrect password.");
    }

    /**
     * only AccountManager should use this -- it is responsible to save
     */ 
    void setPassword(String newPassword)
    {
        passwd = newPassword;
    }

    public String getName() { return name; }
}
