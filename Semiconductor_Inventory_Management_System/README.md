# Semiconductor Inventory Management System (Jakarta EE / EJB + JSF)

This project is a NetBeans-style Ant EAR with EJB (JPA) and WAR (JSF) modules targeting Jakarta EE 10 (GlassFish 7 / Payara 6).

## What was fixed

- Wired Transaction flow and entity mappings (Transactions ↔ Components, Suppliers) and date handling.
- Aligned ID types (Integer) and JSF pages with managed beans.
- Updated JSF tag namespaces to Jakarta Faces 4 URIs.
- Hardened stock update logic for null quantities and invalid inputs.

## Prerequisites

- Java JDK 21 (recommended; 17 also works with GlassFish 7)
- Apache Ant 1.10+
- GlassFish 7.x or Payara 6.x server
- Microsoft SQL Server JDBC Driver (mssql-jdbc-*.jar)

## Server setup (GlassFish)

1. Copy the SQL Server JDBC driver JAR to:
   - `GLASSFISH_HOME/glassfish/domains/domain1/lib/`
2. Create a JDBC connection pool (example):
   - Name: `SemiconductorPool`
   - Resource type: `javax.sql.DataSource`
   - Datasource class: `com.microsoft.sqlserver.jdbc.SQLServerDataSource`
   - Properties: `serverName`, `portNumber`, `databaseName`, `user`, `password`, `encrypt=false`
3. Create a JDBC resource:
   - JNDI name: `jdbc/semiconductorDB`
   - Pool name: `SemiconductorPool`

The persistence unit `semiconductor-pu` uses this JNDI.

## Build

From the project root (the EAR):

```powershell
# Ensure Ant is on PATH first: ant -version should work
ant clean dist
```

Artifacts:
- EAR: `dist/Semiconductor_Inventory_Management_System.ear`
- EJB JAR: `Semiconductor_Inventory_Management_System-ejb/dist/Semiconductor_Inventory_Management_System-ejb.jar`
- WAR: `Semiconductor_Inventory_Management_System-war/dist/Semiconductor_Inventory_Management_System-war.war`

## Deploy

Using the admin console: http://localhost:4848
- Applications → Deploy → select the EAR from `dist/`

Or CLI (adjust paths):

```powershell
# Optional: set GF bin on PATH first
asadmin deploy --force=true dist\Semiconductor_Inventory_Management_System.ear
```

## Run and URLs

- App context (from `glassfish-web.xml` defaults): use direct page URLs:
  - `http://localhost:8080/Semiconductor_Inventory_Management_System-war/pages/index.xhtml`

## Notes for VS Code

The NetBeans Ant project relies on the application server libraries for Jakarta APIs. If the Java Language Server shows `jakarta.* cannot be resolved` in the editor, it’s because those compile-time libraries are not on VS Code’s classpath. Building with Ant still works once Ant and GlassFish are installed. For better editor support, consider:

- Opening in NetBeans or IntelliJ with the GlassFish server configured; or
- Migrating to Maven/Gradle and declare `jakarta.*` APIs as `provided` dependencies.

## Troubleshooting

- `jakarta.* cannot be resolved` in editor: install Ant and build, or configure your IDE to use GlassFish’s libraries.
- `Datasource not found` on deploy: ensure `jdbc/semiconductorDB` exists and the JDBC driver JAR is in the domain `lib/`.
- `Class not found: FacesServlet`: ensure GlassFish 7 is used (Jakarta 10+). Also check `web.xml` uses Jakarta 6.0 schema.
- SQL Server SSL error: add `encrypt=false` or configure certificates properly in JDBC pool.

## Tech stack

- Jakarta EE 10: EJB, JPA, JSF
- GlassFish 7 / Payara 6
- Ant (NetBeans-generated build)
- SQL Server
