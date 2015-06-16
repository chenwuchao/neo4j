/**
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.storemigration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.neo4j.kernel.impl.storemigration.MigrationTestUtils.changeVersionNumber;
import static org.neo4j.kernel.impl.storemigration.MigrationTestUtils.truncateFile;
import static org.neo4j.kernel.impl.storemigration.MigrationTestUtils.truncateToFixedLength;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.helpers.UTF8;
import org.neo4j.kernel.DefaultFileSystemAbstraction;
import org.neo4j.kernel.impl.nioneo.store.FileSystemAbstraction;

public class UpgradableDatabaseTestIT
{
    @Test
    public void shouldAcceptTheStoresInTheSampleDatabaseAsBeingEligibleForUpgrade()
    {

        assertTrue( new UpgradableDatabase( new StoreVersionCheck( fileSystem ) ).storeFilesUpgradeable( new File( workingDirectory, "neostore" ) ) );
    }

    @Test
    public void shouldRejectStoresIfOneFileHasIncorrectVersion() throws IOException
    {
        changeVersionNumber( fileSystem, new File( workingDirectory, "neostore.nodestore.db" ), "v0.9.5" );

        assertFalse( new UpgradableDatabase( new StoreVersionCheck( fileSystem ) ).storeFilesUpgradeable( new File( workingDirectory, "neostore" ) ) );
    }

    @Test
    public void shouldRejectStoresIfOneFileHasNoVersionAsIfNotShutDownCleanly() throws IOException
    {
        truncateFile( fileSystem, new File( workingDirectory, "neostore.nodestore.db" ), "StringPropertyStore v0.9.9" );

        assertFalse( new UpgradableDatabase( new StoreVersionCheck( fileSystem ) ).storeFilesUpgradeable( new File( workingDirectory, "neostore" ) ) );
    }

    @Test
    public void shouldRejectStoresIfOneFileShorterThanExpectedVersionString() throws IOException
    {
        int shortFileLength = 5 /* (RelationshipTypeStore.RECORD_SIZE) */ * 3;
        assertTrue( shortFileLength < UTF8.encode( "StringPropertyStore v0.9.9" ).length );
        truncateToFixedLength( fileSystem, new File( workingDirectory, "neostore.relationshiptypestore.db" ), shortFileLength );

        assertFalse( new UpgradableDatabase( new StoreVersionCheck( fileSystem ) ).storeFilesUpgradeable( new File( workingDirectory, "neostore" ) ) );
    }

    private final FileSystemAbstraction fileSystem = new DefaultFileSystemAbstraction();
    private File workingDirectory;

    @Before
    public void prepareDirectory() throws IOException
    {
        workingDirectory = MigrationTestUtils.findOldFormatStoreDirectory();
    }
}