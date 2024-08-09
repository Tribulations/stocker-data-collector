Certainly! Here’s a revised version of your `README.md` for the `resources/db/migration` folder:

---

# Migration Scripts

This folder contains SQL scripts used for managing the database schema. These scripts serve both as documentation and as a means to facilitate database interactions, such as creating tables, triggers, and other database objects.

## Script Execution

Each script in this directory has been executed at least once to apply the corresponding changes to the database. However, if needed, you can re-run any script using the following command:

```bash
psql -U youruser -d yourdatabase -f src/main/resources/db/migration/name_of_script.sql
```

## Naming Convention

The scripts are named according to the Flyway migration convention, which facilitates potential future migration to Flyway or other schema management tools. The naming format is as follows:

- `V<version>__<description>.sql`

For example:
- `V1__create_initial_tables.sql`
- `V2__add_new_column_to_table.sql`

## Future Migration

In the future, if the project adopts Flyway or a similar migration tool, the scripts will be automatically detected and applied based on their versioning and naming conventions.

---

Feel free to adjust any specific details or commands to better fit your project’s context.