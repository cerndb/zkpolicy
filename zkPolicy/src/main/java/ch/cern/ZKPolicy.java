package ch.cern;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.File;
//import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public final class ZKPolicy {
    private ZKPolicy() {
    }

    /**
     * Main function of the ZK tool
     * 
     * @param args The arguments of the program.
     */
    public static void main(String[] args) throws KeeperException, InterruptedException {
        ZKConnection zkConnection = new ZKConnection();
        ZooKeeper zkeeper = null;
        try {
            zkeeper = zkConnection.connect("localhost");
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        // first addauth for the superuser
        zkeeper.addAuthInfo("digest", "super:super123".getBytes());

        byte[] b = null;
        b = zkeeper.getData("/newznode", null, null);
        ;
        try {
            System.out.println(new String(b, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        ZKTree zktree = new ZKTree(zkeeper);

        // Use lambda function and ZKQuery interface
        // example, just query every node that has at least one acl entry with create
        // permissions
        ZKQuery query = (aclList) -> {
            for (ACL aclElement : aclList) {
                ACLaugment aclAugment = new ACLaugment(aclElement);
                if (aclAugment.hasAdmin()) {
                    return true;
                }

            }
            return false;
        };

        // example 2, print all nodes, export ZNode tree functionality
        ZKQuery exportAll = (aclList) -> {
            return true;
        };

        // example 3, get all nodes that use the digest scheme
        ZKQuery queryDigest = (aclList) -> {
            for (ACL aclElement : aclList) {
                ACLaugment aclAugment = new ACLaugment(aclElement);
                if (aclAugment.getScheme().equals("digest")) {
                    return true;
                }

            }
            return false;
        };

        System.out.println("Example 1");
        System.out.print(zktree.queryTree("/", query));
        System.out.print(zktree.queryFind("/", query));

        System.out.println("Example 2");
        System.out.print(zktree.queryTree("/", exportAll));
        System.out.print(zktree.queryFind("/", exportAll));

        System.out.println("Example 3");
        System.out.print(zktree.queryTree("/", queryDigest));
        System.out.print(zktree.queryFind("/", queryDigest));

        System.out.println("Loading Custom jar from classpath");
        

File file = new File("/home/arvchristos/Documents/CERN/java_implementation/plugins/target/plugins-1.0-SNAPSHOT.jar");
        try {
            URLClassLoader child = new URLClassLoader(
                new URL[] {file.toURI().toURL()},
                ZKPolicy.class.getClassLoader()
                );
            Class classToLoad = Class.forName("org.cern.QueryChristos", true, child);
            //Method method = classToLoad.getDeclaredMethod("myMethod");
            Object instance = classToLoad.newInstance();
            
            //Object result = method.invoke(instance);
            System.out.print(zktree.queryTree("/", (ZKQuery)instance));
        } catch (Exception e) {
            e.printStackTrace();
            //TODO: handle exception
        }
        



    } 

}

