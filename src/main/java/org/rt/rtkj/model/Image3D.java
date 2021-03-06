package org.rt.rtkj.model;

import lombok.Data;
import org.rt.rtkj.dicom.ImagePositionPatientComparator;
import org.rt.rtkj.dicom.Modality;
import org.rt.rtkj.dicom.PatientPosition;
import org.rt.rtkj.dicom.PixelRepresentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class Image3D {
    private List<Image2D> images;
    private boolean sorted = false;
    ImagePositionPatientComparator imagePositionPatientComparator = new ImagePositionPatientComparator();

    public void add(Image2D slice) {
        if (images == null) images = new ArrayList<>();
        if (slice.getSOPInstanceUID().isEmpty()) return;
        images.add(slice);
        sorted = false;
    }

    public Image2D get(int index) throws NullPointerException, IndexOutOfBoundsException {
        return images.get(index);
    }

    public int size() {
        return (images == null) ? 0 : images.size();
    }

    public void sort() {
        images.sort(imagePositionPatientComparator);
        sorted = true;
    }

    public String getFrameOfReferenceUID() {
        if (images.isEmpty()) return "";
        return images.get(0).getFrameOfReferenceUID();
    }

    public Modality getModality() {
        if (images.isEmpty()) return Modality.UNKNOWN;
        return images.get(0).getModality();
    }

    public String getStudyInstanceUID() {
        if (images.isEmpty()) return "";
        return images.get(0).getStudyInstanceUID();
    }

    public PatientPosition getPatientPosition() {
        if (images.isEmpty()) return PatientPosition.UNKOWN;
        return images.get(0).getPatientPosition();
    }

    public double[] getPixelSpacing() {
        if (images.isEmpty()) return null;
        return images.get(0).getPixelSpacing();
    }

    public Optional<double[]> getImagePositionPatient() {
        if (images.isEmpty()) return Optional.empty();
        if (!sorted) sort();
        return Optional.of(images.get(0).getImagePositionPatient());
    }

    public double[] getImageOrientationPatient() {
        if (images.isEmpty()) return null;
        return images.get(0).getImageOrientationPatient();
    }

    public PixelRepresentation getPixelRepresentation() {
        if (images.isEmpty()) return PixelRepresentation.NONE;
        return images.get(0).getPixelRepresentation();
    }

    public int getBitsAllocated() {
        if (images.isEmpty()) return 0;
        return images.get(0).getBitsAllocated();
    }

    public Optional<Double> getValue(int column, int row, int depth) {
        if (!sorted) sort();
        int nd = images.size();
        if (depth < 0 || depth >= nd) return Optional.empty();
        var image = images.get(depth);
        return image.getValue(column, row);
    }

    public Optional<Double> getScaledValue(int column, int row, int depth) {
        if (!sorted) sort();
        int nd = images.size();
        if (depth < 0 || depth >= nd) return Optional.empty();
        var image = images.get(depth);
        return image.getScaledValue(column, row);
    }

    //TODO compute slice thickness
}
