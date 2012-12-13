/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.benchmark;

import java.awt.BasicStroke;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.drools.planner.benchmark.statistic.MillisecondsSpendNumberFormat;
import org.drools.planner.benchmark.statistic.SolverStatistic;
import org.drools.planner.benchmark.statistic.SolverStatisticType;
import org.drools.planner.benchmark.statistic.bestscore.BestScoreStatisticListener;
import org.drools.planner.benchmark.statistic.bestscore.BestScoreStatisticPoint;
import org.drools.planner.config.termination.TerminationConfig;
import org.drools.planner.core.Solver;
import org.drools.planner.core.score.Score;
import org.drools.planner.core.score.definition.ScoreDefinition;
import org.drools.planner.core.solution.Solution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardXYItemLabelGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XStreamAlias("solverBenchmarkSuite")
public class SolverBenchmarkSuite {

    private static final NumberFormat TIME_FORMAT = NumberFormat.getIntegerInstance(Locale.ENGLISH);

    @XStreamOmitField
    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private File benchmarkDirectory = null;
    private File benchmarkInstanceDirectory = null;
    private File solvedSolutionFilesDirectory = null;
    private File solverStatisticFilesDirectory = null;
    @XStreamImplicit(itemFieldName = "solverStatisticType")
    private List<SolverStatisticType> solverStatisticTypeList = null;
    private Comparator<SolverBenchmark> solverBenchmarkComparator = null;

    private Long warmUpTimeMillisSpend = null;
    private Long warmUpSecondsSpend = null;
    private Long warmUpMinutesSpend = null;
    private Long warmUpHoursSpend = null;

    private SolverBenchmark inheritedSolverBenchmark = null;

    @XStreamImplicit(itemFieldName = "solverBenchmark")
    private List<SolverBenchmark> solverBenchmarkList = null;

//    @XStreamImplicit(itemFieldName = "solverBenchmarkSuiteResult")
//    private List<SolverBenchmarkSuiteResult> solverBenchmarkSuiteResultList;

    public File getBenchmarkDirectory() {
        return benchmarkDirectory;
    }

    public void setBenchmarkDirectory(File benchmarkDirectory) {
        this.benchmarkDirectory = benchmarkDirectory;
    }

    public File getBenchmarkInstanceDirectory() {
        return benchmarkInstanceDirectory;
    }

    public void setBenchmarkInstanceDirectory(File benchmarkInstanceDirectory) {
        this.benchmarkInstanceDirectory = benchmarkInstanceDirectory;
    }

    public File getSolvedSolutionFilesDirectory() {
        return solvedSolutionFilesDirectory;
    }

    public void setSolvedSolutionFilesDirectory(File solvedSolutionFilesDirectory) {
        this.solvedSolutionFilesDirectory = solvedSolutionFilesDirectory;
    }

    public File getSolverStatisticFilesDirectory() {
        return solverStatisticFilesDirectory;
    }

    public void setSolverStatisticFilesDirectory(File solverStatisticFilesDirectory) {
        this.solverStatisticFilesDirectory = solverStatisticFilesDirectory;
    }

    public List<SolverStatisticType> getSolverStatisticTypeList() {
        return solverStatisticTypeList;
    }

    public void setSolverStatisticTypeList(List<SolverStatisticType> solverStatisticTypeList) {
        this.solverStatisticTypeList = solverStatisticTypeList;
    }

    public Comparator<SolverBenchmark> getSolverBenchmarkComparator() {
        return solverBenchmarkComparator;
    }

    public void setSolverBenchmarkComparator(Comparator<SolverBenchmark> solverBenchmarkComparator) {
        this.solverBenchmarkComparator = solverBenchmarkComparator;
    }

    public Long getWarmUpTimeMillisSpend() {
        return warmUpTimeMillisSpend;
    }

    public void setWarmUpTimeMillisSpend(Long warmUpTimeMillisSpend) {
        this.warmUpTimeMillisSpend = warmUpTimeMillisSpend;
    }

    public Long getWarmUpSecondsSpend() {
        return warmUpSecondsSpend;
    }

    public void setWarmUpSecondsSpend(Long warmUpSecondsSpend) {
        this.warmUpSecondsSpend = warmUpSecondsSpend;
    }

    public Long getWarmUpMinutesSpend() {
        return warmUpMinutesSpend;
    }

    public void setWarmUpMinutesSpend(Long warmUpMinutesSpend) {
        this.warmUpMinutesSpend = warmUpMinutesSpend;
    }

    public Long getWarmUpHoursSpend() {
        return warmUpHoursSpend;
    }

    public void setWarmUpHoursSpend(Long warmUpHoursSpend) {
        this.warmUpHoursSpend = warmUpHoursSpend;
    }

    public List<SolverBenchmark> getSolverBenchmarkList() {
        return solverBenchmarkList;
    }

    public void setSolverBenchmarkList(List<SolverBenchmark> solverBenchmarkList) {
        this.solverBenchmarkList = solverBenchmarkList;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public void benchmarkingStarted() {
        if (solverBenchmarkList == null || solverBenchmarkList.isEmpty()) {
            throw new IllegalArgumentException(
                    "Configure at least 1 <solverBenchmark> in the <solverBenchmarkSuite> configuration.");
        }
        Set<String> nameSet = new HashSet<String>(solverBenchmarkList.size());
        Set<SolverBenchmark> noNameBenchmarkSet = new LinkedHashSet<SolverBenchmark>(solverBenchmarkList.size());
        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
            if (solverBenchmark.getName() != null) {
                boolean unique = nameSet.add(solverBenchmark.getName());
                if (!unique) {
                    throw new IllegalStateException("The benchmark name (" + solverBenchmark.getName()
                            + ") is used in more than 1 benchmark.");
                }
            } else {
                noNameBenchmarkSet.add(solverBenchmark);
            }
            if (inheritedSolverBenchmark != null) {
                solverBenchmark.inherit(inheritedSolverBenchmark);
            }
            solverBenchmark.validate();
            solverBenchmark.resetSolverBenchmarkResultList();
        }
        int generatedNameIndex = 0;
        for (SolverBenchmark solverBenchmark : noNameBenchmarkSet) {
            String generatedName = "Config_" + generatedNameIndex;
            while (nameSet.contains(generatedName)) {
                generatedNameIndex++;
                generatedName = "Config_" + generatedNameIndex;
            }
            solverBenchmark.setName(generatedName);
            generatedNameIndex++;
        }
        if (benchmarkDirectory == null) {
            throw new IllegalArgumentException("The benchmarkDirectory (" + benchmarkDirectory + ") must not be null.");
        }
        benchmarkDirectory.mkdirs();
        if (solvedSolutionFilesDirectory == null) {
            String timestampDirectory = new SimpleDateFormat("yyyy-MM-dd_HHmmss").format(new Date());
            benchmarkInstanceDirectory = new File(benchmarkDirectory, timestampDirectory);
        }
        benchmarkInstanceDirectory.mkdirs();
        if (solvedSolutionFilesDirectory == null) {
            solvedSolutionFilesDirectory = new File(benchmarkInstanceDirectory, "solved");
        }
        solvedSolutionFilesDirectory.mkdirs();
        if (solverStatisticFilesDirectory == null) {
            solverStatisticFilesDirectory = new File(benchmarkInstanceDirectory, "statistic");
        }
        solverStatisticFilesDirectory.mkdirs();
        if (solverBenchmarkComparator == null) {
            solverBenchmarkComparator = new TotalScoreSolverBenchmarkComparator();
        }
//        resetSolverBenchmarkSuiteResultList();
    }

//    private void resetSolverBenchmarkSuiteResultList() {
//        solverBenchmarkSuiteResultList = new ArrayList<SolverBenchmarkSuiteResult>();
//        Map<File, SolverBenchmarkSuiteResult> unsolvedSolutionFileToSuiteResultMap
//                = new LinkedHashMap<File, SolverBenchmarkSuiteResult>();
//        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
//            for (File unsolvedSolutionFile : solverBenchmark.getUnsolvedSolutionFileList()) {
//                if (!unsolvedSolutionFileToSuiteResultMap.containsKey(unsolvedSolutionFile)) {
//                    SolverBenchmarkSuiteResult suiteResult = new SolverBenchmarkSuiteResult();
//                    suiteResult.setUnsolvedSolutionFile(unsolvedSolutionFile);
//                    suiteResult.setSolverBenchmarkResultList(new ArrayList<SolverBenchmarkResult>(
//                            solverBenchmarkList.size()));
//                    solverBenchmarkSuiteResultList.add(suiteResult);
//                    unsolvedSolutionFileToSuiteResultMap.put(unsolvedSolutionFile, suiteResult);
//                }
//            }
//        }
//    }

    public void benchmark(XStream xStream) { // TODO refactor out xstream
        benchmarkingStarted();
        // LinkedHashMap because order of unsolvedSolutionFile should be respected in output
        Map<File, List<SolverStatistic>> unsolvedSolutionFileToStatisticMap = new LinkedHashMap<File, List<SolverStatistic>>();
        if (warmUpTimeMillisSpend != null || warmUpSecondsSpend != null || warmUpMinutesSpend != null
                || warmUpHoursSpend != null) {
            logger.info("================================================================================");
            logger.info("Warming up");
            logger.info("================================================================================");
            long warmUpTimeMillisSpendTotal = 0L;
            if (warmUpTimeMillisSpend != null) {
                warmUpTimeMillisSpendTotal += warmUpTimeMillisSpend;
            }
            if (warmUpSecondsSpend != null) {
                warmUpTimeMillisSpendTotal += warmUpSecondsSpend * 1000L;
            }
            if (warmUpMinutesSpend != null) {
                warmUpTimeMillisSpendTotal += warmUpMinutesSpend * 60000L;
            }
            if (warmUpHoursSpend != null) {
                warmUpTimeMillisSpendTotal += warmUpHoursSpend * 3600000L;
            }
            long startingTimeMillis = System.currentTimeMillis();
            long timeLeft = warmUpTimeMillisSpendTotal;
            Iterator<SolverBenchmark> solverBenchmarkIt = solverBenchmarkList.iterator();
            int overallResultIndex = 0;
            while (timeLeft > 0L) {
                if (!solverBenchmarkIt.hasNext()) {
                    solverBenchmarkIt = solverBenchmarkList.iterator();
                    overallResultIndex++;
                }
                SolverBenchmark solverBenchmark = solverBenchmarkIt.next();
                List<SolverBenchmarkResult> solverBenchmarkResultList = solverBenchmark.getSolverBenchmarkResultList();
                int resultIndex = overallResultIndex % solverBenchmarkResultList.size();
                SolverBenchmarkResult result = solverBenchmarkResultList.get(resultIndex);
                TerminationConfig originalTerminationConfig = solverBenchmark.getSolverConfig().getTerminationConfig();
                TerminationConfig tmpTerminationConfig = originalTerminationConfig.clone();
                tmpTerminationConfig.shortenMaximumTimeMillisSpendTotal(timeLeft);
                solverBenchmark.getSolverConfig().setTerminationConfig(tmpTerminationConfig);
                Solver solver = solverBenchmark.getSolverConfig().buildSolver();
                File unsolvedSolutionFile = result.getUnsolvedSolutionFile();
                Solution unsolvedSolution = readUnsolvedSolution(xStream, unsolvedSolutionFile);
                solver.setPlanningProblem(unsolvedSolution);
                solver.solve();
                solverBenchmark.getSolverConfig().setTerminationConfig(originalTerminationConfig);
                long timeSpend = System.currentTimeMillis() - startingTimeMillis;
                timeLeft = warmUpTimeMillisSpendTotal - timeSpend;
            }
            logger.info("================================================================================");
            logger.info("Finished warmUp");
            logger.info("================================================================================");
        }
        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
            for (SolverBenchmarkResult result : solverBenchmark.getSolverBenchmarkResultList()) {
                // Intentionally create a fresh solver for every result to reset Random, tabu lists, ...
                Solver solver = solverBenchmark.getSolverConfig().buildSolver();
                
                File unsolvedSolutionFile = result.getUnsolvedSolutionFile();
                Solution unsolvedSolution = readUnsolvedSolution(xStream, unsolvedSolutionFile);
                solver.setPlanningProblem(unsolvedSolution);
                List<SolverStatistic> statisticList = getOrCreateStatisticList(unsolvedSolutionFileToStatisticMap, unsolvedSolutionFile);
                for (SolverStatistic statistic : statisticList) {
                    statistic.addListener(solver, solverBenchmark.getName());
                }
                solver.solve();
                result.setTimeMillisSpend(solver.getTimeMillisSpend());
                Solution solvedSolution = solver.getBestSolution();
                result.setScore(solvedSolution.getScore());
                for (SolverStatistic statistic : statisticList) {
                    statistic.removeListener(solver, solverBenchmark.getName());
                }
                writeSolvedSolution(xStream, solverBenchmark, result, solvedSolution);
            }
        }
        benchmarkingEnded(xStream, unsolvedSolutionFileToStatisticMap);
    }

    private List<SolverStatistic> getOrCreateStatisticList(
            Map<File, List<SolverStatistic>> unsolvedSolutionFileToStatisticMap, File unsolvedSolutionFile) {
        if (solverStatisticTypeList == null) {
            return Collections.emptyList();
        }
        List<SolverStatistic> statisticList = unsolvedSolutionFileToStatisticMap.get(unsolvedSolutionFile);
        if (statisticList == null) {
            statisticList = new ArrayList<SolverStatistic>(solverStatisticTypeList.size());
            for (SolverStatisticType solverStatisticType : solverStatisticTypeList) {
                statisticList.add(solverStatisticType.create());
            }
            unsolvedSolutionFileToStatisticMap.put(unsolvedSolutionFile, statisticList);
        }
        return statisticList;
    }

    private Solution readUnsolvedSolution(XStream xStream, File unsolvedSolutionFile) {
        Solution unsolvedSolution;
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(unsolvedSolutionFile), "utf-8");
            unsolvedSolution = (Solution) xStream.fromXML(reader);
        } catch (XStreamException e) {
            throw new IllegalArgumentException("Problem reading unsolvedSolutionFile: " + unsolvedSolutionFile, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem reading unsolvedSolutionFile: " + unsolvedSolutionFile, e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        return unsolvedSolution;
    }

    private void writeSolvedSolution(XStream xStream, SolverBenchmark solverBenchmark, SolverBenchmarkResult result,
            Solution solvedSolution) {
        if (solvedSolutionFilesDirectory == null) {
            return;
        }
        File solvedSolutionFile = null;
        String baseName = FilenameUtils.getBaseName(result.getUnsolvedSolutionFile().getName());
        String solverBenchmarkName = solverBenchmark.getName().replaceAll(" ", "_").replaceAll("[^\\w\\d_\\-]", "");
        String scoreString = result.getScore().toString().replaceAll("[\\/ ]", "_");
        String timeString = TIME_FORMAT.format(result.getTimeMillisSpend()) + "ms";
        solvedSolutionFile = new File(solvedSolutionFilesDirectory, baseName + "_" + solverBenchmarkName
                + "_score" + scoreString + "_time" + timeString + ".xml");
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(solvedSolutionFile), "utf-8");
            xStream.toXML(solvedSolution, writer);
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem writing solvedSolutionFile: " + solvedSolutionFile, e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public void benchmarkingEnded(XStream xStream, Map<File, List<SolverStatistic>> unsolvedSolutionFileToStatisticMap) {
        determineRankings();
        // 2 lines at 80 chars per line give a max of 160 per entry
        StringBuilder htmlFragment = new StringBuilder(unsolvedSolutionFileToStatisticMap.size() * 160);
        htmlFragment.append("  <h1>Summary</h1>\n");
        htmlFragment.append(writeBestScoreSummaryChart());
        htmlFragment.append(writeTimeSpendSummaryChart());
        // TODO scalability summary chart must be n based instead of score based (the latter is misleading)
//        htmlFragment.append(writeScalabilitySummaryChart());
        htmlFragment.append(writeBestScoreSummaryTable());
        htmlFragment.append("  <h1>Statistics</h1>\n");
        for (Map.Entry<File, List<SolverStatistic>> entry : unsolvedSolutionFileToStatisticMap.entrySet()) {
            File unsolvedSolutionFile = entry.getKey();
            List<SolverStatistic> statisticList = entry.getValue();
            String baseName = FilenameUtils.getBaseName(unsolvedSolutionFile.getName());
            htmlFragment.append("  <h2>").append(baseName).append("</h2>\n");
            for (SolverStatistic statistic : statisticList) {
                htmlFragment.append(statistic.writeStatistic(solverStatisticFilesDirectory, baseName));
            }
        }
        writeHtmlOverview(htmlFragment);
        // TODO Temporarily disabled because it crashes because of http://jira.codehaus.org/browse/XSTR-666
        // writeBenchmarkResult(xStream);
    }

    private void determineRankings() {
        List<SolverBenchmark> sortedSolverBenchmarkList = new ArrayList<SolverBenchmark>(solverBenchmarkList);
        Collections.sort(sortedSolverBenchmarkList, solverBenchmarkComparator);
        Collections.reverse(sortedSolverBenchmarkList); // Best results first, worst results last
        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
            solverBenchmark.setRanking(sortedSolverBenchmarkList.indexOf(solverBenchmark));
        }
    }

    private CharSequence writeBestScoreSummaryChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
            ScoreDefinition scoreDefinition = solverBenchmark.getSolverConfig().getScoreDefinitionConfig()
                    .buildScoreDefinition();
            for (SolverBenchmarkResult result : solverBenchmark.getSolverBenchmarkResultList()) {
                Score score = result.getScore();
                Double scoreGraphValue = scoreDefinition.translateScoreToGraphValue(score);
                String solverLabel = solverBenchmark.getName();
                if (solverBenchmark.getRanking() == 0) {
                    solverLabel += " (winner)";
                }
                dataset.addValue(scoreGraphValue, solverLabel, result.getUnsolvedSolutionFile().getName());
            }
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Best score summary (higher score is better)", "Data", "Score",
                dataset, PlotOrientation.VERTICAL, true, true, false
        );
        CategoryItemRenderer renderer = ((CategoryPlot) chart.getPlot()).getRenderer();
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        BufferedImage chartImage = chart.createBufferedImage(1024, 768);
        File chartSummaryFile = new File(solverStatisticFilesDirectory, "bestScoreSummary.png");
        OutputStream out = null;
        try {
            out = new FileOutputStream(chartSummaryFile);
            ImageIO.write(chartImage, "png", out);
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem writing graphStatisticFile: " + chartSummaryFile, e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        return "  <h2>Best score summary chart</h2>\n"
                + "  <img src=\"" + chartSummaryFile.getName() + "\"/>\n";
    }

    private CharSequence writeTimeSpendSummaryChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
            for (SolverBenchmarkResult result : solverBenchmark.getSolverBenchmarkResultList()) {
                long timeMillisSpend = result.getTimeMillisSpend();
                String solverLabel = solverBenchmark.getName();
                dataset.addValue(timeMillisSpend, solverLabel, result.getUnsolvedSolutionFile().getName());
            }
        }
        CategoryAxis xAxis = new CategoryAxis("Data");
        NumberAxis yAxis = new NumberAxis("Time millis spend");
        yAxis.setNumberFormatOverride(new MillisecondsSpendNumberFormat());
        BarRenderer renderer = new BarRenderer();
        ItemLabelPosition positiveItemLabelPosition = new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER);
        renderer.setBasePositiveItemLabelPosition(positiveItemLabelPosition);
        ItemLabelPosition negativeItemLabelPosition = new ItemLabelPosition(
                ItemLabelAnchor.OUTSIDE6, TextAnchor.TOP_CENTER);
        renderer.setBaseNegativeItemLabelPosition(negativeItemLabelPosition);
        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator(
                StandardCategoryItemLabelGenerator.DEFAULT_LABEL_FORMAT_STRING, new MillisecondsSpendNumberFormat()));
        renderer.setBaseItemLabelsVisible(true);
        CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis,
                renderer);
        plot.setOrientation(PlotOrientation.VERTICAL);
        JFreeChart chart = new JFreeChart("Time spend summary (lower time is better)", JFreeChart.DEFAULT_TITLE_FONT,
                plot, true);
        BufferedImage chartImage = chart.createBufferedImage(1024, 768);
        File chartSummaryFile = new File(solverStatisticFilesDirectory, "timeSpendSummary.png");
        OutputStream out = null;
        try {
            out = new FileOutputStream(chartSummaryFile);
            ImageIO.write(chartImage, "png", out);
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem writing graphStatisticFile: " + chartSummaryFile, e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        return "  <h2>Time spend summary chart</h2>\n"
                + "  <img src=\"" + chartSummaryFile.getName() + "\"/>\n";
    }

    private CharSequence writeScalabilitySummaryChart() {
        NumberAxis xAxis = new NumberAxis("Score");
        xAxis.setInverted(true);
        NumberAxis yAxis = new NumberAxis("Time millis spend");
        yAxis.setNumberFormatOverride(new MillisecondsSpendNumberFormat());
        XYPlot plot = new XYPlot(null, xAxis, yAxis, null);
        int seriesIndex = 0;
        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
            XYSeries series = new XYSeries(solverBenchmark.getName());
            ScoreDefinition scoreDefinition = solverBenchmark.getSolverConfig().getScoreDefinitionConfig()
                    .buildScoreDefinition();
            for (SolverBenchmarkResult result : solverBenchmark.getSolverBenchmarkResultList()) {
                Long timeMillisSpend = result.getTimeMillisSpend();
                Score score = result.getScore();
                Double scoreGraphValue = scoreDefinition.translateScoreToGraphValue(score);
                if (scoreGraphValue != null) {
                    series.add(scoreGraphValue, timeMillisSpend);
                }
            }
            XYSeriesCollection seriesCollection = new XYSeriesCollection();
            seriesCollection.addSeries(series);
            plot.setDataset(seriesIndex, seriesCollection);
            XYItemRenderer renderer = new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES_AND_LINES);
            // Use dashed line
            renderer.setSeriesStroke(0, new BasicStroke(
                    1.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{2.0f, 6.0f}, 0.0f
            ));
            plot.setRenderer(seriesIndex, renderer);
            seriesIndex++;
        }
        plot.setOrientation(PlotOrientation.VERTICAL);
        JFreeChart chart = new JFreeChart("Scalability summary (lower and lefter is better)",
                JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        BufferedImage chartImage = chart.createBufferedImage(1024, 768);
        File chartSummaryFile = new File(solverStatisticFilesDirectory, "scalabilitySummary.png");
        OutputStream out = null;
        try {
            out = new FileOutputStream(chartSummaryFile);
            ImageIO.write(chartImage, "png", out);
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem writing graphStatisticFile: " + chartSummaryFile, e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        return "  <h2>Scalability summary chart</h2>\n"
                + "  <img src=\"" + chartSummaryFile.getName() + "\"/>\n";
    }

    private CharSequence writeBestScoreSummaryTable() {
        StringBuilder htmlFragment = new StringBuilder(solverBenchmarkList.size() * 160);
        htmlFragment.append("  <h2>Best score summary table</h2>\n");
        htmlFragment.append("  <table border=\"1\">\n");
        htmlFragment.append("    <tr><th/>");
        if (inheritedSolverBenchmark != null && inheritedSolverBenchmark.getUnsolvedSolutionFileList() != null) {
            for (File unsolvedSolutionFile : inheritedSolverBenchmark.getUnsolvedSolutionFileList()) {
                htmlFragment.append("<th>").append(unsolvedSolutionFile.getName()).append("</th>");
            }
        }
        htmlFragment.append("<th>Average</th><th>Ranking</th></tr>\n");
        boolean oddLine = true;
        for (SolverBenchmark solverBenchmark : solverBenchmarkList) {
            String backgroundColor = solverBenchmark.getRanking() == 0 ? "Yellow" : oddLine ? "White" : "LightGray";
            htmlFragment.append("    <tr style=\"background-color: ").append(backgroundColor).append("\"><th>")
                    .append(solverBenchmark.getName()).append("</th>");
            for (SolverBenchmarkResult result : solverBenchmark.getSolverBenchmarkResultList()) {
                Score score = result.getScore();
                htmlFragment.append("<td>").append(score.toString()).append("</td>");
            }
            htmlFragment.append("<td>").append(solverBenchmark.getAverageScore().toString())
                    .append("</td><td>").append(solverBenchmark.getRanking()).append("</td>");
            htmlFragment.append("</tr>\n");
            oddLine = !oddLine;
        }
        htmlFragment.append("  </table>\n");
        return htmlFragment.toString();
    }

    private void writeHtmlOverview(CharSequence htmlFragment) {
        File htmlOverviewFile = new File(solverStatisticFilesDirectory, "index.html");
        Writer writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(htmlOverviewFile), "utf-8");
            writer.append("<html>\n");
            writer.append("<head>\n");
            writer.append("  <title>Statistic</title>\n");
            writer.append("</head>\n");
            writer.append("<body>\n");
            writer.append(htmlFragment);
            writer.append("</body>\n");
            writer.append("</html>\n");
        } catch (IOException e) {
            throw new IllegalArgumentException("Problem writing htmlOverviewFile: " + htmlOverviewFile, e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    public void writeBenchmarkResult(XStream xStream) {
        File benchmarkResultFile = new File(benchmarkInstanceDirectory, "benchmarkResult.xml");
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(benchmarkResultFile), "utf-8");
            xStream.toXML(this, writer);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("This JVM does not support utf-8 encoding.", e);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(
                    "Could not create benchmarkResultFile (" + benchmarkResultFile + ").", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

}