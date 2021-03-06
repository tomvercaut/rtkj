package org.rt.rtkj.dicom;

import lombok.extern.log4j.Log4j2;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This factory provides a general
 */
@Log4j2
public class DicomFactory {

    public static DicomObject read(String pathname) throws IOException, DicomException {
        File file = new File(pathname);
        return read(file);
    }

    public static DicomObject read(Path path) throws IOException, DicomException {
        if (path == null) return new DicomObject();
        return read(path.toFile());
    }

    public static DicomObject read(File file) throws IOException, DicomException {
        DicomObject dicomObject = new DicomObject();
        if (file == null || !file.isFile() || !file.canRead()) {
            return dicomObject;
        }

        DicomInputStream dis = new DicomInputStream(new BufferedInputStream(new FileInputStream(file)));
        Attributes meta = dis.readFileMetaInformation();
        var bo = (dis.bigEndian()) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        Attributes dataset = dis.readDataset(-1, -1);
        if (!dataset.contains(Tag.SOPClassUID)) {
            log.error("DICOM stream doesn't contain a SOPClassUID");
            return dicomObject;
        }

        String sopClassUID = dataset.getString(Tag.SOPClassUID);
        switch (sopClassUID) {
            case UID.CTImageStorage:
                var ct = Reader.ct(meta, dataset, bo);
                ct.ifPresent(dicomObject::set);
                break;
            case UID.PositronEmissionTomographyImageStorage:
                var pt = Reader.pt(meta, dataset, bo);
                pt.ifPresent(dicomObject::set);
                break;
            case UID.RTStructureSetStorage:
                var ss = Reader.structureSet(meta, dataset, bo);
                ss.ifPresent(dicomObject::set);
                break;
            case UID.RTDoseStorage:
                var rtd = Reader.rtDose(meta, dataset, bo);
                rtd.ifPresent(dicomObject::set);
                break;
            case UID.SpatialRegistrationStorage:
                var sr = Reader.spatialRegistration(meta, dataset, bo);
                sr.ifPresent(dicomObject::set);
                break;
            default:
                log.error(String.format("Trying to read an unsupported DICOM file [SOPClassUID: %s]", sopClassUID));
                break;
        }
        dicomObject.setPathname(file.getAbsolutePath());
        return dicomObject;
    }

    public static List<DicomObject> read(List<String> pathnames) throws IOException, DicomException {
        if (pathnames == null || pathnames.isEmpty()) return new ArrayList<>();
        List<DicomObject> list = new ArrayList<>(pathnames.size());
        for (String s : pathnames) {
            DicomObject doj = read(s);
            list.add(doj);
        }
        return list;
    }

    public static boolean write(String pathname, RTDose dose) throws IOException {
        File file = new File(pathname);
        return write(file, dose);
    }

    public static boolean write(Path path, RTDose dose) throws IOException {
        return write(path.toFile(), dose);
    }

    public static boolean write(File file, RTDose dose) throws IOException {
        if (file == null) {
            return false;
        }
        DicomOutputStream dos = new DicomOutputStream(file);
        var optAttr = Writer.rtdose(dose);
        final String errMsg = String.format("Unable to write RTDose file %s", file.toString());
        if (optAttr.isEmpty()) {
            log.error(errMsg);
            return false;
        }

        dos.writeDataset(null, optAttr.get());
        dos.close();
        return true;
    }
}