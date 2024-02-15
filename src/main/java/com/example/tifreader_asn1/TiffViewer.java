package com.example.tifreader_asn1;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TiffViewer extends JFrame implements ActionListener {

    private ImagePanel colorPanel, grayPanel;
    private JFileChooser fileChooser;
    private BufferedImage originalImage, grayImage;
    private int step = 0;
    private JLabel stepLabel, methodLabel;

    private JButton nextButton;


    //set up panel for the GUI
    public TiffViewer() {
        super("Tiff Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        colorPanel = new ImagePanel();
        grayPanel = new ImagePanel();
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(colorPanel);
        panel.add(grayPanel);
        add(panel, BorderLayout.CENTER);

        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new TiffFileFilter());

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open File");
        openItem.addActionListener(this);
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(this);
        menu.add(openItem);
        menu.add(exitItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        nextButton = new JButton("Next");
        nextButton.addActionListener(this);

        methodLabel = new JLabel("Method: Original");

        stepLabel = new JLabel("Step: " + step); // Initialize stepLabel

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.add(nextButton);
        bottomPanel.add(Box.createRigidArea(new Dimension(10, 0))); // Add some space between the button and the label
        bottomPanel.add(stepLabel);
        bottomPanel.add(methodLabel);

        add(bottomPanel, BorderLayout.SOUTH);

        setSize(800, 600); // Set the size of the frame
        setVisible(true);
    }


    //set up actions for the GUI
    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("Open File")) {
            int returnVal = fileChooser.showOpenDialog(TiffViewer.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try {
                    originalImage = ImageIO.read(file);
                    grayImage = convertToGrayscale(originalImage);
                    colorPanel.setImage(originalImage);
                    grayPanel.setImage(grayImage);
                    step = 0;
                    stepLabel.setText("Step: " + step);
                    methodLabel.setText("Method: Original");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else if (command.equals("Exit")) {
            System.exit(0);
        } else if (command.equals("Next")) {
            step++;
            switch (step) {
                case 1:
                    // Reduce brightness by 50%
                    BufferedImage reducedBrightnessColorImage = reduceBrightness(new BufferedImage(originalImage.getColorModel(), originalImage.copyData(null), originalImage.isAlphaPremultiplied(), null), 0.5f);
                    BufferedImage reducedBrightnessGrayImage = reduceBrightness(new BufferedImage(grayImage.getColorModel(), grayImage.copyData(null), grayImage.isAlphaPremultiplied(), null), 0.5f);
                    colorPanel.setImage(reducedBrightnessColorImage);
                    grayPanel.setImage(reducedBrightnessGrayImage);
                    methodLabel.setText("Method: Reduced Brightness");
                    break;
                case 2:
                    // Apply ordered dithering
                    BufferedImage ditheredImage = orderedDithering(new BufferedImage(grayImage.getColorModel(), grayImage.copyData(null), grayImage.isAlphaPremultiplied(), null));
                    colorPanel.setImage(new BufferedImage(grayImage.getColorModel(), grayImage.copyData(null), grayImage.isAlphaPremultiplied(), null)); // Display original grayscale image on left
                    grayPanel.setImage(ditheredImage); // Display dithered image on right
                    methodLabel.setText("Method: Ordered Dithering");
                    break;
                case 3:
                    // Apply auto level
                    BufferedImage autoLeveledColorImage = autoLevel(new BufferedImage(originalImage.getColorModel(), originalImage.copyData(null), originalImage.isAlphaPremultiplied(), null));
                    colorPanel.setImage(originalImage); // Display original colored image on left
                    grayPanel.setImage(autoLeveledColorImage); // Display auto leveled image on right
                    methodLabel.setText("Method: Auto Level");
                    break;
                default:
                    // Reset to original image
                    try {
                        originalImage = ImageIO.read(fileChooser.getSelectedFile());
                        grayImage = convertToGrayscale(originalImage);
                        step = 0;
                        methodLabel.setText("Method: Original");
                        colorPanel.setImage(originalImage);
                        grayPanel.setImage(grayImage);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
            }
            stepLabel.setText("Step: " + step);
        }
    }

    // Convert an image to grayscale
    private BufferedImage convertToGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                //calculating the gray value with the unique equation
                int grayValue = (int) (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());

                // Set the pixel in the grayscale image
                grayImage.setRGB(x, y, (grayValue << 16) | (grayValue << 8) | grayValue);
            }
        }

        return grayImage;
    }

    // Reduce the brightness of an image by 50% input
    private BufferedImage reduceBrightness(BufferedImage image, float scale) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage reducedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = reducedImage.getGraphics();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int red = Math.max((int) (color.getRed() * scale), 0);
                int green = Math.max((int) (color.getGreen() * scale), 0);
                int blue = Math.max((int) (color.getBlue() * scale), 0);

                Color newColor = new Color(red, green, blue);
                reducedImage.setRGB(x, y, newColor.getRGB());
            }
        }

        return reducedImage;
    }

    // Apply ordered dithering to an image using a 4x4 matrix
    public BufferedImage orderedDithering(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage ditheredImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        Graphics g = ditheredImage.getGraphics();

        // Define matrix
        int[][] ditherMatrix = {
                { 1 , 9, 3,  11},
                {13,  5, 15,  7},
                { 4, 12,  2, 10},
                {16,  8, 14,  6}
        };

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(image.getRGB(x, y));
                int grayValue = (int) (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());

                // Apply the dither matrix
                int threshold = 17 * ditherMatrix[x % 4][y % 4]; // Adjust the multiplier for the larger matrix
                int binaryValue = grayValue > threshold ? 255 : 0;

                // Set the pixel in the dithered image
                ditheredImage.setRGB(x, y, (binaryValue << 16) | (binaryValue << 8) | binaryValue);
            }
        }

        return ditheredImage;
    }



    //Apply auto level to an image

    //idea of extracting and calculating the color was referenced from
    //https://stackoverflow.com/questions/5569659/java2-dimensional-array-with-color-components-and-histogram-interval
    public BufferedImage autoLevel(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Calculate histograms for each color channel
        int[][] histogram = new int[3][256];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = image.getRGB(i, j);
                histogram[0][((rgb >> 16) & 0xff)]++; // Red
                histogram[1][((rgb >> 8) & 0xff)]++;  // Green
                histogram[2][(rgb & 0xff)]++;         // Blue
            }
        }

        // Find the minimum and maximum values in each histogram
        int[] minVal = new int[3], maxVal = new int[3];
        for (int k = 0; k < 3; k++) {
            while (histogram[k][minVal[k]] == 0) minVal[k]++;
            maxVal[k] = 255;
            while (histogram[k][maxVal[k]] == 0) maxVal[k]--;
        }

        // Apply auto level to each color channel
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int rgb = image.getRGB(i, j);
                int red = (((rgb >> 16) & 0xff - minVal[0]) * 255 / (maxVal[0] - minVal[0]));
                int green = (((rgb >> 8) & 0xff - minVal[1]) * 255 / (maxVal[1] - minVal[1]));
                int blue = ((rgb & 0xff - minVal[2]) * 255 / (maxVal[2] - minVal[2]));

                // Increase contrast by 30%
                red = Math.min(255, (int)(red * 1.4));
                green = Math.min(255, (int)(green * 1.4));
                blue = Math.min(255, (int)(blue * 1.4));

                rgb = (red << 16) | (green << 8) | blue;
                outputImage.setRGB(i, j, rgb);
            }
        }

        return outputImage;
    }


    public static void main(String[] args) {
        new TiffViewer();
    }
}

class ImagePanel extends JPanel {

    private BufferedImage image;

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        }
    }
}

// A custom file filter class that only accepts .tif files
class TiffFileFilter extends javax.swing.filechooser.FileFilter {

    @Override
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
            return extension.equals("tif");
        }

        return false;
    }

    @Override
    public String getDescription() {
        return "TIFF Files (*.tif)";
    }

    private String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}

