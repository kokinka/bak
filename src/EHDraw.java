/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class EHDraw extends Application {

    static EvolutionTree strom = new EvolutionTree();
    static GraphicsContext gc;
    static Stage primaryStage;
    static boolean gui = true;// -nogui to turn off
    static boolean optimize = false; // -opt
    static boolean export_greedy = false; //-exportgreedy
    static boolean export_ilp = false; //-exportilp
    static boolean export_minimized = false; //-exportminimized
    static boolean draw_svg = false; // -drawsvg
    static boolean export_crossings = false; //-exportcrossingscount
    static String input; //-input:input.history
    static String svg_output = "output.svg";
    static String ilp_output = "output.lp";
    static String greedy_output = "output.greedy";
    static String minimized_output = "minimized.history";
    static String crossings_output = "crossings.txt";
    static String load_settings = null; //-settings:filename
    static String load_lp = null;
    static ComboBox pickGene;
    static Button removeMeta;
    static ComboBox drawC;
    static ComboBox highlightedC;
    static ComboBox transparentC;
    static TextField lineWF;
    static TextField nameF;
    static ColorPicker colorP;
    static Button useDefault;

    //static Settings s=new Settings();
    //Scanner in = new Scanner(System.in);
    @Override
    public void start(Stage _primaryStage) throws FileNotFoundException, IOException {
        primaryStage = _primaryStage;


        VBox controlsVBox = new VBox(10);
        VBox globalsVBox = new VBox();
        HBox root = new HBox(10);

        final Canvas cnv = new Canvas(Settings.width, Settings.height);
        ZoomablePane cnvPane = new ZoomablePane();
        cnvPane.getChildren().add(cnv);

        root.getChildren().add(controlsVBox);
        root.getChildren().add(cnvPane);
        root.getChildren().add(globalsVBox);

        Group group = new Group();
        group.getChildren().add(root);
        Scene scene = new Scene(group, Settings.width + 400, Settings.height);

        SceneGestures sceneGestures = new SceneGestures(cnvPane);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());
        scene.addEventFilter(ScrollEvent.ANY, sceneGestures.getOnScrollEventHandler());
        primaryStage.setTitle(Settings.title);
        primaryStage.setScene(scene);
        primaryStage.show();
        gc = cnv.getGraphicsContext2D();


        Button openButton = new Button("Open");
        Button optimizeButton = new Button("Optimize");
        Button exportILPButton = new Button("Export ILP");
        Button importILPButton = new Button("Import ILP");
        Button saveIMGButton = new Button("Save Img");
        Button importSettingsButton = new Button("Import Settings");
        Button exportSettingsButton = new Button("Export Settings");
        Button redrawButton = new Button("Redraw");
        Button crossingsButton = new Button("Minimize crossings");
        Button exportHistoryButton = new Button("Export history");

        openButton.setOnAction(new OpenButtonHandler(primaryStage));
        optimizeButton.setOnAction(new OptimizeButtonHandler());
        saveIMGButton.setOnAction(new SaveImgButtonHandler());
        redrawButton.setOnAction(new redrawButtonHandler());
        importSettingsButton.setOnAction(new importSettingsButtonHandler());
        exportSettingsButton.setOnAction(new exportSettingsButtonHandler());
        exportILPButton.setOnAction(new exportILPButtonHandler());
        importILPButton.setOnAction(new importILPButtonHandler());
        crossingsButton.setOnAction(new CrossingsButtonHandler());
        exportHistoryButton.setOnAction(new ExportHistory());


        ObservableList<javafx.scene.Node> controls = controlsVBox.getChildren();
        controls.add(openButton);
        controls.add(saveIMGButton);
        controls.add(importSettingsButton);
        controls.add(exportSettingsButton);
        controls.add(optimizeButton);
        controls.add(exportILPButton);
        controls.add(importILPButton);
        controls.add(redrawButton);
        controls.add(crossingsButton);
        controls.add(exportHistoryButton);

        Label timeDiffL = new Label("Time diff");
        final TextField timeDiffF = new TextField(Double.toString(Settings.time_diff));
        HBox timeDiffH = new HBox(10);
        timeDiffH.getChildren().addAll(timeDiffL, timeDiffF);
        timeDiffF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                if (validDouble(timeDiffF.getText())) {
                    Settings.time_diff = Double.parseDouble(timeDiffF.getText());
                    updated();
                }
            }

        });

        Label widthL = new Label("Width");
        final TextField widthF = new TextField(Integer.toString(Settings.width));
        HBox widthH = new HBox(10);
        widthH.getChildren().addAll(widthL, widthF);
        widthF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                String s = widthF.getText();
                if (validInteger(s)) {
                    Settings.width = Integer.parseInt(s);
                    cnv.setWidth(Settings.width);
                    strom.calcScale();
                    draw();
                }
            }
        });

        Label heightL = new Label("Height");
        final TextField heightF = new TextField(Integer.toString(Settings.height));
        HBox heightH = new HBox(10);
        heightH.getChildren().addAll(heightL, heightF);
        heightF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                String s = heightF.getText();
                if (validInteger(s)) {
                    Settings.height = Integer.parseInt(s);
                    cnv.setHeight(Settings.height);
                    draw();
                }
            }
        });

        Label nodeGapL = new Label("Node Gap");
        final TextField nodeGapF = new TextField(Integer.toString(Settings.node_gap));
        HBox nodeGapH = new HBox(10);
        nodeGapH.getChildren().addAll(nodeGapL, nodeGapF);

        nodeGapF.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Settings.node_gap = Integer.parseInt(nodeGapF.getText());
                updated();
            }
        });
        Label chromosomeGapL = new Label("Chromosome Gap");
        final TextField chromosomeGapF = new TextField(Integer.toString(Settings.chromosome_gap));
        HBox chromosomeGapH = new HBox(10);
        chromosomeGapH.getChildren().addAll(chromosomeGapL, chromosomeGapF);

        chromosomeGapF.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Settings.chromosome_gap = Integer.parseInt(chromosomeGapF.getText());
                updated();
            }
        });
        Label lineGapL = new Label("Line Gap");
        final TextField lineGapF = new TextField(Integer.toString(Settings.line_gap));
        HBox lineGapH = new HBox(10);
        lineGapH.getChildren().addAll(lineGapL, lineGapF);
        lineGapF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                if (validInteger(lineGapF.getText())) {
                    Settings.line_gap = Integer.parseInt(lineGapF.getText());
                    updated();
                }
            }
        });

        Label lineSizeL = new Label("Line Size");
        final TextField lineSizeF = new TextField(Integer.toString(Settings.line_size));
        HBox lineSizeH = new HBox(10);
        lineSizeH.getChildren().addAll(lineSizeL, lineSizeF);
        lineSizeF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                String s = lineSizeF.getText();
                if (validInteger(s)) {
                    Settings.line_size = Integer.parseInt(s);
                    updated();
                }
            }
        });

        Label satL = new Label("Saturation");
        final TextField satF = new TextField(Double.toString(Settings.saturation));
        HBox satH = new HBox(10);
        satH.getChildren().addAll(satL, satF);
        satF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                String s = satF.getText();
                if (validDouble(s)) {
                    Settings.saturation = Double.parseDouble(s);
                    Settings.calcColors();
                    updated();
                }
            }
        });

        Label briL = new Label("Brightness");
        final TextField briF = new TextField(Double.toString(Settings.brightness));
        HBox briH = new HBox(10);
        briH.getChildren().addAll(briL, briF);
        briF.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                String s = briF.getText();
                if (validDouble(s)) {
                    Settings.brightness = Double.parseDouble(s);
                    Settings.calcColors();
                    updated();
                }
            }
        });

        Label optL = new Label("Optimization");
        final TextField optF = new TextField(Integer.toString(Settings.optimized));
        HBox optH = new HBox(10);
        optH.getChildren().addAll(optL, optF);
        optF.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                String s = optF.getText();
                Settings.optimized = Integer.parseInt(s);
                strom.optimize();
                updated();
            }

        });

        Label nonhighlightedL = new Label("Non highlighted");
        ColorPicker nonhighlightedP = new ColorPicker((Color) Settings.nonhighlighted);
        HBox nonhighlightedH = new HBox(10);
        nonhighlightedH.getChildren().addAll(nonhighlightedL, nonhighlightedP);
        nonhighlightedP.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Settings.nonhighlighted = nonhighlightedP.getValue();
                updated();
            }
        });

        final ToggleButton redraw = new ToggleButton("Redraw");
        redraw.setSelected(Settings.redraw);
        redraw.setOnAction(new EventHandler() {

            @Override
            public void handle(Event event) {
                Settings.redraw = redraw.isSelected();
            }
        });


        pickGene = new ComboBox();


        Label drawL = new Label("Draw");
        drawC = new ComboBox();
        HBox drawH = new HBox(10);
        drawH.getChildren().addAll(drawL, drawC);


        Label highlightedL = new Label("Highlighted");
        highlightedC = new ComboBox();
        HBox highlightedB = new HBox(10);
        highlightedB.getChildren().addAll(highlightedL, highlightedC);


        Label transparentL = new Label("Transparent");
        transparentC = new ComboBox();
        HBox transparentH = new HBox(10);
        transparentH.getChildren().addAll(transparentL, transparentC);


        Label lineWL = new Label("Line Width");
        lineWF = new TextField(Integer.toString(Settings.line_size));
        HBox lineWH = new HBox(10);
        lineWH.getChildren().addAll(lineWL, lineWF);


        Label nameL = new Label("Name");
        nameF = new TextField();
        HBox nameH = new HBox(10);
        nameH.getChildren().addAll(nameL, nameF);


        Label colourL = new Label("Colour");
        colorP = new ColorPicker();
        HBox colorH = new HBox(10);
        colorH.getChildren().addAll(colourL, colorP);


        removeMeta = new Button("Delete gene meta");

        useDefault = new Button("Delete all meta");

        HBox metaButtonsH = new HBox(10);
        metaButtonsH.getChildren().addAll(removeMeta, useDefault);
        useDefault.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Settings.clearGeneMeta();
                metaclear();
                updated();
            }
        });
        removeMeta.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                int gene = (int) pickGene.getValue();
                Settings.removeMeta(gene);
                updatedgene(gene);
                updated();
            }
        });
        metaclear();
        attach_listeners();
        globalsVBox.getChildren().addAll(timeDiffH, widthH, heightH, nodeGapH, chromosomeGapH, lineGapH, lineSizeH, satH, briH, optH, nonhighlightedH, redraw, pickGene, drawH, transparentH, highlightedB, lineWH, nameH, colorH, metaButtonsH);


    }

    private static void metaclear() {
        dettach_listeners();
        pickGene.getItems().clear();
        pickGene.getItems().add("None");
        pickGene.getItems().addAll("Default");
        pickGene.getItems().addAll(strom.getGenes());
        pickGene.setValue("None");
        update_specific_panel();
        attach_listeners();
    }

    private static void updated() {
        if (Settings.redraw) {
            draw();
        }
        update_specific_panel();
    }

    private static void attach_listeners() {
        nameF.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                int gene = (int) pickGene.getValue();
                Settings.set_gene_name(gene, nameF.getText());
                updatedgene(gene);
                updated();
            }

        });
        colorP.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                int gene = (int) pickGene.getValue();
                Settings.set_gene_col(gene, colorP.getValue());
                updatedgene(gene);
                updated();
            }
        });
        lineWF.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                int gene = (int) pickGene.getValue();
                Settings.set_gene_width(gene, Integer.parseInt(lineWF.getText()));
                updatedgene(gene);
                updated();
            }

        });
        transparentC.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Boolean val = Boolean.parseBoolean(transparentC.getValue().toString());
                if (pickGene.getValue() == "Default") {
                    Settings.default_transparent = val;
                } else {
                    int gene = (int) pickGene.getValue();
                    Settings.set_gene_trans(gene, val);
                    updatedgene(gene);

                }
                updated();
            }
        });
        highlightedC.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Boolean val = Boolean.parseBoolean(highlightedC.getValue().toString());
                if (pickGene.getValue() == "Default") {
                    Settings.default_highlighted = val;
                } else {
                    int gene = (int) pickGene.getValue();
                    Settings.set_gene_highlighted(gene, val);
                    updatedgene(gene);
                }
                updated();
            }
        });
        drawC.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                Boolean val = Boolean.parseBoolean(drawC.getValue().toString());
                if (pickGene.getValue() == "Default") {
                    Settings.default_draw = val;
                } else {
                    int gene = (int) pickGene.getValue();
                    Settings.set_gene_draw(gene, val);
                    updatedgene(gene);

                }
                updated();
            }
        });
        pickGene.setOnAction(new EventHandler() {
            @Override
            public void handle(Event event) {
                update_specific_panel();

            }
        });
    }

    private static void update_specific_panel() {
        dettach_listeners();
        if (pickGene.getValue() == "None") {
            removeMeta.setDisable(true);
            colorP.setDisable(true);
            nameF.setDisable(true);
            lineWF.setDisable(true);
            transparentC.setDisable(true);
            highlightedC.setDisable(true);
            drawC.setDisable(true);
        } else {
            if (pickGene.getValue() == "Default") {
                colorP.setDisable(true);
                nameF.setDisable(true);
                lineWF.setDisable(true);
                transparentC.setDisable(false);
                highlightedC.setDisable(false);
                drawC.setDisable(false);

                transparentC.getItems().clear();
                transparentC.getItems().add("True");
                transparentC.getItems().add("False");
                transparentC.setValue("False");
                if (Settings.default_transparent) {
                    transparentC.setValue("True");
                }
                highlightedC.getItems().clear();
                highlightedC.getItems().add("True");
                highlightedC.getItems().add("False");
                highlightedC.setValue("False");
                if (Settings.default_highlighted) {
                    highlightedC.setValue("True");
                }

                drawC.getItems().clear();
                drawC.getItems().add("True");
                drawC.getItems().add("False");
                drawC.setValue("False");
                if (Settings.default_draw) {
                    drawC.setValue("True");
                }

            } else {
                int gene = Integer.parseInt(pickGene.getValue().toString());
                updatedgene(gene);
            }
        }
        attach_listeners();
    }

    private static void dettach_listeners() {
        nameF.setOnAction(null);
        colorP.setOnAction(null);
        lineWF.setOnAction(null);
        transparentC.setOnAction(null);
        highlightedC.setOnAction(null);
        drawC.setOnAction(null);
        pickGene.setOnAction(null);

    }

    private static void updatedgene(int gene) {
        dettach_listeners();
        removeMeta.setDisable(false);
        colorP.setDisable(false);
        nameF.setDisable(false);
        lineWF.setDisable(false);
        transparentC.setDisable(false);
        highlightedC.setDisable(false);
        drawC.setDisable(false);

        transparentC.getItems().clear();
        transparentC.getItems().add("True");
        transparentC.getItems().add("False");


        highlightedC.getItems().clear();
        highlightedC.getItems().add("True");
        highlightedC.getItems().add("False");


        drawC.getItems().clear();
        drawC.getItems().add("True");
        drawC.getItems().add("False");
        nameF.clear();
        lineWF.clear();
        if (!Settings.gene_meta.containsKey(gene)) {
            transparentC.getItems().add(0, "<Default>");
            highlightedC.getItems().add(0, "<Default>");
            drawC.getItems().add(0, "<Default>");
            transparentC.setValue("<Default>");
            highlightedC.setValue("<Default>");
            drawC.setValue("<Default>");
            nameF.clear();
            lineWF.clear();
        } else {
            if (Settings.is_draw(gene)) {
                drawC.setValue("True");
            } else {
                drawC.setValue("False");
            }
            if (Settings.isHighlighted(gene)) {
                highlightedC.setValue("True");
            } else {
                highlightedC.setValue("False");
            }
            if (Settings.isTransparent(gene)) {
                transparentC.setValue("True");
            } else {
                transparentC.setValue("False");
            }
            String s = Settings.gene_name(gene);
            if (s != null) {
                nameF.setText(s);
            }
            Integer lw = Settings.gene_meta.get(gene).getLine_width();
            if (lw != null) {
                lineWF.setText(lw.toString());
            }

        }
        colorP.setValue((Color) Settings.gene_color(gene));
        attach_listeners();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        for (String s : args) {
            String[] splitted = s.split(":");
            switch (splitted[0]) {
                case "-load_settings":
                    load_settings = splitted[1];
                    break;
                case "-exportgreedy":
                    export_greedy = true;
                    break;
                case "-exportilp":
                    export_ilp = true;
                    break;
                case "-exportminimized":
                    export_minimized = true;
                case "-exportcrossings":
                    export_crossings = true;
                    break;
                case "-drawsvg":
                    draw_svg = true;
                    break;
                case "-input":
                    input = splitted[1];
                    break;
                case "-nogui":
                    gui = false;
                    break;
                case "-opt":
                    optimize = true;
                    Settings.optimized = Integer.parseInt(splitted[1]);
                    break;
                case "-greedy_output":
                    greedy_output = splitted[1];
                    break;
                case "-ilp_output":
                    ilp_output = splitted[1];
                    break;
                case "-svg_output":
                    svg_output = splitted[1];
                    break;
                case "-load_lp":
                    load_lp = splitted[1];
                    break;
                case "-minimized_output":
                    minimized_output = splitted[1];
                    break;
                case "-crossings_output":
                    crossings_output = splitted[1];
                    break;
            }
        }
        if (gui) {
            launch(args);
        } else {
            File f = new File(input);
            strom.load(f);
            if (optimize) {
                strom.findBlocks();
            }
            if (export_greedy) {
                File file = new File(greedy_output);
                file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                strom.exportBlocks(bw);
                fw.close();

            }
            if (export_ilp) {
                File file = new File(ilp_output);
                file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                strom.exportILP(bw);
                fw.close();
            }
            if (export_minimized) {
                File file = new File(minimized_output);
                file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                strom = strom.level_by_level_sweep();
                strom.exportCrossings(bw);
                fw.close();
                export_minimized = false;
            }
            if (export_crossings) {
                File file = new File(crossings_output);
                file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(strom.countCrossings(strom.getRoot()).toString());
                bw.close();
                fw.close();
                export_crossings = false;
            }
            if (load_settings != null) {
                Settings.loadXML(load_settings);
            }
            if (load_lp != null) {
                File lp = new File(load_lp);
                strom.loadILP(lp);
            }
            if (draw_svg) {
                DrawFactory drawF = new SVGDrawFactory();
                drawF.clear();
                strom.print(drawF);
                File file = new File(svg_output);
                file.delete();
                file.createNewFile();
                drawF.export(file);
            }
            //System.exit(0);
        }
    }

    private static void draw() {
        DrawFactory drawF = new FXDrawFactory(gc);
        drawF.clear();
        strom.print(drawF);
    }

    private static boolean validDouble(String s) {
        return true;
    }

    private static boolean validInteger(String s) {
        return true;
    }

    private static class OpenButtonHandler implements EventHandler<ActionEvent> {

        private final Stage primaryStage;

        public OpenButtonHandler(Stage _primaryStage) {
            this.primaryStage = _primaryStage;
        }

        @Override
        public void handle(ActionEvent event) {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("History file (*.history)", "*.history");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(primaryStage);
            Settings.title = file.getName();
            primaryStage.setTitle(Settings.title);
            try {
                strom = new EvolutionTree();
                strom.load(file);
                draw();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
            }
            metaclear();

        }


    }

    private static class SaveImgButtonHandler implements EventHandler<ActionEvent> {

        public SaveImgButtonHandler() {

        }

        @Override
        public void handle(ActionEvent event) {
            /**
             * DrawFactory drawF = new SVGDrawFactory(); strom.print(drawF);
             * drawF.export();
             *
             */
            FileChooser fileChooser1 = new FileChooser();
            fileChooser1.setTitle("Save Image");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SVG files (*.svg)", "*.svg");
            fileChooser1.getExtensionFilters().add(extFilter);
            File file = fileChooser1.showSaveDialog(primaryStage);

            DrawFactory drawF = new SVGDrawFactory();
            strom.print(drawF);
            drawF.export(file);
        }
    }

    private static class redrawButtonHandler implements EventHandler<ActionEvent> {

        public redrawButtonHandler() {

        }

        @Override
        public void handle(ActionEvent event) {
            draw();
        }
    }

    private static class importSettingsButtonHandler implements EventHandler<ActionEvent> {

        public importSettingsButtonHandler() {
        }

        @Override
        public void handle(ActionEvent event) {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("xml files (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(primaryStage);
            Settings.loadXML(file);
            updated();

        }
    }

    private static class exportSettingsButtonHandler implements EventHandler<ActionEvent> {

        public exportSettingsButtonHandler() {

        }

        @Override
        public void handle(ActionEvent event) {
            FileChooser fileChooser1 = new FileChooser();
            fileChooser1.setTitle("Export Settings");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("xml files (*.xml)", "*.xml");
            fileChooser1.getExtensionFilters().add(extFilter);
            File file = fileChooser1.showSaveDialog(primaryStage);
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
            }
            Settings.exportXML(file);

        }
    }

    private static class OptimizeButtonHandler implements EventHandler<ActionEvent> {

        public OptimizeButtonHandler() {

        }

        @Override
        public void handle(ActionEvent event) {
            strom.optimize();
            updated();
        }
    }

    private static class exportILPButtonHandler implements EventHandler<ActionEvent> {

        public exportILPButtonHandler() {

        }

        @Override
        public void handle(ActionEvent event) {
            try {
                FileChooser fileChooser1 = new FileChooser();
                fileChooser1.setTitle("Export ILP");
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("ILP files (*.lp)", "*.lp");
                fileChooser1.getExtensionFilters().add(extFilter);
                File file = fileChooser1.showSaveDialog(primaryStage);
                file.delete();
                file.createNewFile();
                FileWriter fw = null;
                try {
                    fw = new FileWriter(file);
                } catch (IOException ex) {
                    Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
                }
                BufferedWriter bw = new BufferedWriter(fw);
                try {
                    strom.exportILP(bw);
                    fw.close();
                } catch (IOException ex) {
                    Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static class importILPButtonHandler implements EventHandler<ActionEvent> {

        public importILPButtonHandler() {
        }

        @Override
        public void handle(ActionEvent event) {
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Solution files (*.sol)", "*.sol");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(primaryStage);
            try {
                strom.loadILP(file);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
            }
            updated();
        }
    }

    private static class CrossingsButtonHandler implements EventHandler<ActionEvent> {

        public CrossingsButtonHandler() {

        }

        @Override
        public void handle(ActionEvent event) {
            strom = strom.level_by_level_sweep();
            draw();
        }

    }

    private static class ExportHistory implements EventHandler<ActionEvent> {

        public ExportHistory() {

        }

        @Override
        public void handle(ActionEvent event) {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Export history");
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HISTORY files (*.history)", "*.history");
                fileChooser.getExtensionFilters().add(extFilter);
                File file = fileChooser.showSaveDialog(primaryStage);
                file.delete();
                file.createNewFile();
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                strom.exportCrossings(bw);
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(EHDraw.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
