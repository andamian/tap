/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2009.                            (c) 2009.
 *  Government of Canada                 Gouvernement du Canada
 *  National Research Council            Conseil national de recherches
 *  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
 *  All rights reserved                  Tous droits réservés
 *
 *  NRC disclaims any warranties,        Le CNRC dénie toute garantie
 *  expressed, implied, or               énoncée, implicite ou légale,
 *  statutory, of any kind with          de quelque nature que ce
 *  respect to the software,             soit, concernant le logiciel,
 *  including without limitation         y compris sans restriction
 *  any warranty of merchantability      toute garantie de valeur
 *  or fitness for a particular          marchande ou de pertinence
 *  purpose. NRC shall not be            pour un usage particulier.
 *  liable in any event for any          Le CNRC ne pourra en aucun cas
 *  damages, whether direct or           être tenu responsable de tout
 *  indirect, special or general,        dommage, direct ou indirect,
 *  consequential or incidental,         particulier ou général,
 *  arising from the use of the          accessoire ou fortuit, résultant
 *  software.  Neither the name          de l'utilisation du logiciel. Ni
 *  of the National Research             le nom du Conseil National de
 *  Council of Canada nor the            Recherches du Canada ni les noms
 *  names of its contributors may        de ses  participants ne peuvent
 *  be used to endorse or promote        être utilisés pour approuver ou
 *  products derived from this           promouvoir les produits dérivés
 *  software without specific prior      de ce logiciel sans autorisation
 *  written permission.                  préalable et particulière
 *                                       par écrit.
 *
 *  This file is part of the             Ce fichier fait partie du projet
 *  OpenCADC project.                    OpenCADC.
 *
 *  OpenCADC is free software:           OpenCADC est un logiciel libre ;
 *  you can redistribute it and/or       vous pouvez le redistribuer ou le
 *  modify it under the terms of         modifier suivant les termes de
 *  the GNU Affero General Public        la “GNU Affero General Public
 *  License as published by the          License” telle que publiée
 *  Free Software Foundation,            par la Free Software Foundation
 *  either version 3 of the              : soit la version 3 de cette
 *  License, or (at your option)         licence, soit (à votre gré)
 *  any later version.                   toute version ultérieure.
 *
 *  OpenCADC is distributed in the       OpenCADC est distribué
 *  hope that it will be useful,         dans l’espoir qu’il vous
 *  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
 *  without even the implied             GARANTIE : sans même la garantie
 *  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
 *  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
 *  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
 *  General Public License for           Générale Publique GNU Affero
 *  more details.                        pour plus de détails.
 *
 *  You should have received             Vous devriez avoir reçu une
 *  a copy of the GNU Affero             copie de la Licence Générale
 *  General Public License along         Publique GNU Affero avec
 *  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
 *  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
 *                                       <http://www.gnu.org/licenses/>.
 *
 *  $Revision: 4 $
 *
 ************************************************************************
 */

package ca.nrc.cadc.tap.writer.format;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.nrc.cadc.dali.util.DefaultFormat;
import ca.nrc.cadc.dali.util.Format;
import ca.nrc.cadc.dali.util.UUIDFormat;
import ca.nrc.cadc.tap.schema.ColumnDesc;
import ca.nrc.cadc.tap.schema.ParamDesc;
import ca.nrc.cadc.uws.Job;

/**
 * Returns a Formatter for a given data type.
 *
 */
public class DefaultFormatFactory implements FormatFactory
{
    protected Job job;
    
    public DefaultFormatFactory() { }

    public void setJob(Job job)
    {
        this.job = job;
    }

    @Override
    public List<Format<Object>> getFormats(List<ParamDesc> selectList)
    {
        List<Format<Object>> formats = new ArrayList<Format<Object>>();
        for (ParamDesc paramDesc : selectList)
        {
            if (paramDesc != null)
            {
                formats.add(getFormat(paramDesc));
            }
        }
        return formats;
    }

    /**
     * Return the default format when no type-specific one is found.
     *
     * @return a DefaultFormat
     */
    protected Format<Object> getDefaultFormat()
    {
        return new DefaultFormat();
    }

    /**
     * Create a formatter for the specified parameter description. The default implementation simply
     * checks the datatype in the argument ParamDesc and then calls the appropriate (public) get<type>Formatter
     * method. Subclasses should override this method if they need to support additional datatypes
     * (as specified in the TapSchema: tap_schema.columns.datatype).
     *
     * @param columnDesc
     * @return
     */
    @Override
    public Format<Object> getFormat(ColumnDesc columnDesc)
    {
        String datatype = columnDesc.getDatatype();
        if (datatype.equalsIgnoreCase("adql:INTEGER"))
            return getIntegerFormat(columnDesc);

        if (datatype.equalsIgnoreCase("adql:BIGINT"))
            return getLongFormat(columnDesc);

        if (datatype.equalsIgnoreCase("adql:DOUBLE"))
            return getDoubleFormat(columnDesc);

        if (datatype.equalsIgnoreCase("adql:CHAR") || datatype.equalsIgnoreCase("adql:VARCHAR"))
            return getStringFormat(columnDesc);

        if (datatype.equalsIgnoreCase("adql:BINARY") || datatype.equalsIgnoreCase("adql:VARBINARY"))
            return getByteArrayFormat(columnDesc);

        if (datatype.equalsIgnoreCase("adql:CLOB"))
            return getClobFormat(columnDesc);
        
        if (datatype.equalsIgnoreCase("adql:BLOB"))
            return getBlobFormat(columnDesc);

        // TAP or DALI xtypes
        if ( datatype.equalsIgnoreCase("timestamp") // DALI-1.1
                || datatype.equalsIgnoreCase("adql:TIMESTAMP")) // TAP-1.0
            return new UTCTimestampFormat();

        if ( datatype.equalsIgnoreCase("point")) // DALI-1.1
            return getPointFormat(columnDesc);
        
        if (datatype.equalsIgnoreCase("circle")) // DALI-1.1
            return getCircleFormat(columnDesc);
        
        if (datatype.equalsIgnoreCase("polygon")) // DALI-1.1
            return getPolygonFormat(columnDesc);
        
        if (datatype.equalsIgnoreCase("adql:POINT")) // TAP-1.0
            return getPointFormat(columnDesc);
        
        if (datatype.equalsIgnoreCase("adql:REGION")) // TAP-1.0
            return getRegionFormat(columnDesc);
        
        if (datatype.equalsIgnoreCase("interval")
               || datatype.equalsIgnoreCase("adql:proto:INTERVAL"))
                return getIntervalFormat(columnDesc);
        
        if (datatype.equalsIgnoreCase("UUID"))
            return getUUIDFormat(columnDesc);

        // VOTable datatypes in the tap_schema.columns.datatype
        if (datatype.equalsIgnoreCase("votable:int"))
            if (columnDesc.getArraysize() != null && columnDesc.getArraysize() > 1)
                return getIntArrayFormat(columnDesc);
            else
                return getIntegerFormat(columnDesc);

        if (datatype.equalsIgnoreCase("votable:long"))
            if (columnDesc.getArraysize() != null && columnDesc.getArraysize() > 1)
                return getLongArrayFormat(columnDesc);
            else
                return getLongFormat(columnDesc);

        if (datatype.equalsIgnoreCase("votable:float"))
            if (columnDesc.getArraysize() != null && columnDesc.getArraysize() > 1)
                return getFloatArrayFormat(columnDesc);
            else
                return getRealFormat(columnDesc);

        if (datatype.equalsIgnoreCase("votable:double"))
            if (columnDesc.getArraysize() != null && columnDesc.getArraysize() > 1)
                return getDoubleArrayFormat(columnDesc);
            else
                return getDoubleFormat(columnDesc);

        return getDefaultFormat();
    }

    @Override
    public Format<Object> getFormat(ParamDesc paramDesc)
    {
        if (paramDesc.columnDesc != null)
            return getFormat(paramDesc.columnDesc);

        String datatype = paramDesc.datatype;

        if (datatype == null)
            return getDefaultFormat();

        if ( datatype.equalsIgnoreCase("timestamp") // DALI-1.1
                || datatype.equalsIgnoreCase("adql:TIMESTAMP")) // TAP-1.0
            return new UTCTimestampFormat();

        if ( datatype.equalsIgnoreCase("point")) // DALI-1.1
            return getPointFormat(paramDesc.columnDesc);
        
        if (datatype.equalsIgnoreCase("circle")) // DALI-1.1
            return getCircleFormat(paramDesc.columnDesc);
        
        if (datatype.equalsIgnoreCase("polygon")) // DALI-1.1
            return getPolygonFormat(paramDesc.columnDesc);
        
        if (datatype.equalsIgnoreCase("adql:POINT")) // TAP-1.0
            return getPointFormat(paramDesc.columnDesc);
        
        // circle function call in select list
        if (datatype.equalsIgnoreCase("adql:REGION") 
                && paramDesc.name.equalsIgnoreCase("circle")) // TAP-1.0 HACK
            return getCircleFormat(paramDesc.columnDesc);
        
        if (datatype.equalsIgnoreCase("adql:REGION")) // TAP-1.0
            return getRegionFormat(paramDesc.columnDesc);
        
        if (datatype.equalsIgnoreCase("interval")
               || datatype.equalsIgnoreCase("adql:proto:INTERVAL"))
                return getIntervalFormat(paramDesc.columnDesc);

        return getDefaultFormat();
    }

    /**
     * @param columnDesc
     * @return a DefaultFormat
     */
    protected Format<Object> getIntegerFormat(ColumnDesc columnDesc)
    {
        return getDefaultFormat();
    }

    /**
     * @param columnDesc
     * @return a DefaultFormat
     */
    protected Format<Object> getRealFormat(ColumnDesc columnDesc)
    {
        return getDefaultFormat();
    }

    /**
     * @param columnDesc
     * @return a DefaultFormat
     */
    protected Format<Object> getDoubleFormat(ColumnDesc columnDesc)
    {
        return getDefaultFormat();
    }


    /**
     * @param columnDesc
     * @return a DefaultFormat
     */
    protected Format<Object> getLongFormat(ColumnDesc columnDesc)
    {
        return getDefaultFormat();
    }

    /**
     * @param columnDesc
     * @return a DefaultFormat
     */
    protected Format<Object> getStringFormat(ColumnDesc columnDesc)
    {
        return getDefaultFormat();
    }

    /**
     * @param columnDesc
     * @return a ByteArrayFormat
     */
    protected Format<Object> getByteArrayFormat(ColumnDesc columnDesc)
    {
        return new ByteArrayFormat();
    }

    /**
     * @param columnDesc
     * @return an IntArrayFormat
     */
    protected Format<Object> getIntArrayFormat(ColumnDesc columnDesc)
    {
        return new IntArrayFormat();
    }

    /**
     * @param columnDesc
     * @return an LongArrayFormat
     */
    protected Format<Object> getLongArrayFormat(ColumnDesc columnDesc)
    {
        return new LongArrayFormat();
    }

    /**
     * @param columnDesc
     * @return an FloatArrayFormat
     */
    protected Format<Object> getFloatArrayFormat(ColumnDesc columnDesc)
    {
        return new FloatArrayFormat();
    }

    /**
     * @param columnDesc
     * @return an DoubleArrayFormat
     */
    protected Format<Object> getDoubleArrayFormat(ColumnDesc columnDesc)
    {
        return new DoubleArrayFormat();
    }


    /**
     * @param columnDesc
     * @return a UTCTimestampFormat
     */
    protected Format<Object> getTimestampFormat(ColumnDesc columnDesc)
    {
        return new UTCTimestampFormat();
    }

    /**
     * @param columnDesc
     * @throws UnsupportedOperationException
     */
    protected Format<Object> getPointFormat(ColumnDesc columnDesc)
    {
        throw new UnsupportedOperationException("no formatter for column " + columnDesc.getColumnName());
    }

    /**
     * @param columnDesc
     * @throws UnsupportedOperationException
     */
    protected Format<Object> getCircleFormat(ColumnDesc columnDesc)
    {
        throw new UnsupportedOperationException("no formatter for column " + columnDesc.getColumnName());
    }
    
    /**
     * @param columnDesc
     * @throws UnsupportedOperationException
     */
    protected Format<Object> getPolygonFormat(ColumnDesc columnDesc)
    {
        throw new UnsupportedOperationException("no formatter for column " + columnDesc.getColumnName());
    }
    
    /**
     * @param columnDesc
     * @throws UnsupportedOperationException
     */
    protected Format<Object> getRegionFormat(ColumnDesc columnDesc)
    {
        throw new UnsupportedOperationException("no formatter for column " + columnDesc.getColumnName());
    }
    
    /**
     * @param columnDesc
     * @throws UnsupportedOperationException
     */
    protected Format<Object> getIntervalFormat(ColumnDesc columnDesc)
    {
        throw new UnsupportedOperationException("no formatter for column " + columnDesc.getColumnName());
    }

    /**
     * @param columnDesc
     * @return a DefaultFormat
     * @throws UnsupportedOperationException
     */
    protected Format<Object> getBlobFormat(ColumnDesc columnDesc)
    {
        return new ByteArrayFormat();
    }
    
    /**
     * @param columnDesc
     * @return a DefaultFormat
     * @throws UnsupportedOperationException
     */
    protected Format<Object> getClobFormat(ColumnDesc columnDesc)
    {
        return getDefaultFormat();
    }

    /**
     * @param columnDesc
     * @return a DefaultFormat
     * @throws UnsupportedOperationException
     */
    protected Format<Object> getUUIDFormat(ColumnDesc columnDesc)
    {
        return getDefaultFormat();
    }
}
