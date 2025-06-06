package javajava;
/* Java utility classes */
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
/* Logging classes */
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/* OSHI Hardware/Software classes */
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.Display;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.PhysicalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

/**
 * Capturing current environment details
 */
public final class EnvironmentCapturingClass {
    /**
     * pointer for all logs
     */
    private static final Logger LOGGER = LogManager.getLogger(EnvironmentCapturingClass.class);
    /**
     * Hardware info
     */
    private final static HardwareAbstractionLayer hardware = new SystemInfo().getHardware(); // NOPMD by Daniel Popiniuc on 17.04.2025, 17:39
    /**
     * OS info
     */
    private final static OperatingSystem operatingSystem = new SystemInfo().getOperatingSystem(); // NOPMD by Daniel Popiniuc on 17.04.2025, 17:39
    /**
     * standard String
     */
    private final static String strName = "Name";

    /**
     * Display details
     * 
     * @param crtDisplay current Display object
     * @return String
     */
    private static String digestDisplayDetails(final Display crtDisplay) {
        final Map<String, Object> arrayAttributes = new ConcurrentHashMap<>();
        final String[] arrayDetails = crtDisplay.toString().replaceAll("[^a-zA-Z0-9\\s]", "").split("\n");
        for (final String crtLine : arrayDetails) {
            final String strSlimLine = crtLine.trim();
            if (crtLine.endsWith(" in") && crtLine.contains(" cm ")) {
                final int intCmPos = strSlimLine.indexOf(" cm ");
                arrayAttributes.put("Physical Dimensions [in]", strSlimLine.substring(0, intCmPos));
                final int intInPos = strSlimLine.indexOf(" in");
                arrayAttributes.put("Physical Dimensions [cm]", strSlimLine.substring(intCmPos + 4, intInPos));
            }
            if (strSlimLine.startsWith("Monitor Name")) {
                arrayAttributes.put("Monitor Name", strSlimLine.replace("Monitor Name ", ""));
            }
            if (crtLine.trim().startsWith("Preferred Timing Clock")) {
                final int intClockLen = "Preferred Timing Clock".length();
                final int intPixelPos = strSlimLine.indexOf("Active Pixels");
                arrayAttributes.put("Preferred Timing Clock", strSlimLine.substring(intClockLen, intPixelPos).trim());
                arrayAttributes.put("Active Pixels", strSlimLine.substring(intPixelPos).replace("Active Pixels ", "").trim());
            }
            if (crtLine.trim().startsWith("Range Limits")) {
                arrayAttributes.put("Range Limits", strSlimLine.replace("Range Limits ", ""));
            }
            if (crtLine.trim().startsWith("Serial Number")) {
                arrayAttributes.put("Serial Number", strSlimLine.replace("Serial Number ", ""));
            }
        }
        return Common.getMapIntoJsonString(arrayAttributes);
    }

    /**
     * Capturing current Environment details
     * 
     * @return String
     */
    public static String getCurrentEnvironmentDetails() {
        String strFeedback = JavaJavaLocalization.getMessage("i18nAppInformationCapturing");
        LOGGER.info(strFeedback);
        final StringBuilder strJsonString = new StringBuilder(100);
        strJsonString.append(String.format("\"Hardware\":{\"CPU\":%s,\"RAM\":%s,\"Storage\":{%s},\"GPU(s)\":%s,\"Monitors\":%s}", getDetailsAboutCentralPowerUnit(), getDetailsAboutRandomAccessMemory(), getDetailsAboutAvailableStoragePartitions(), getDetailsAboutGraphicCards(), getDetailsAboutMonitor()));
        strFeedback = JavaJavaLocalization.getMessage("i18nAppInformationHardwareCaptured");
        LOGGER.debug(strFeedback);
        strJsonString.append(String.format(",\"Software\":{\"OS\":%s,\"Java\":%s,\"User\":%s}", getDetailsAboutOperatingSystem(), getDetailsAboutSoftwarePlatformJava(), getDetailsAboutSoftwareUser()));
        strFeedback = JavaJavaLocalization.getMessage("i18nAppInformationSoftwareCaptured");
        LOGGER.debug(strFeedback);
        strJsonString.append(String.format(",\"Application\":{\"Dependencies\":%s}", DependenciesClass.getCurrentDependencies()));
        strFeedback = JavaJavaLocalization.getMessage("i18nAppInformationApplicationCaptured");
        LOGGER.debug(strFeedback);
        strJsonString.append(String.format(",\"Environment\":{\"Computer\":\"%s\",\"User\":\"%s\"}", System.getenv("COMPUTERNAME"), System.getenv("USERNAME")));
        strFeedback = JavaJavaLocalization.getMessage("i18nAppInformationEnvironmentCaptured");
        LOGGER.info(strFeedback);
        return String.format("{%s}", strJsonString);
    }

    /**
     * List with all partitions
     * 
     * @return String
     */
    private static String getDetailsAboutAvailableStoragePartitions() {
        final StringBuilder strJsonString = new StringBuilder(50);
        final FileSystem osFileSystem = operatingSystem.getFileSystem();
        final List<OSFileStore> osFileStores = osFileSystem.getFileStores();
        strJsonString.append("\"Partition(s)\":[");
        int strCounterDisk = 0;
        for(final OSFileStore fileStore : osFileStores) {
            if (strCounterDisk > 0) {
                strJsonString.append(',');
            }
            strJsonString.append(Common.getMapIntoJsonString(Map.of(
                "Description", fileStore.getDescription(),
                "Label", fileStore.getLabel(),
                "Logical Volume", fileStore.getLogicalVolume(),
                "Mount", fileStore.getMount().replace("\\", "\\\\"),
                strName, fileStore.getName(),
                "Options", fileStore.getOptions(),
                "Total Space", FormatUtil.formatBytes(fileStore.getTotalSpace()),
                "Type", fileStore.getType(),
                "UUID", fileStore.getUUID(),
                "Usable Space", FormatUtil.formatBytes(fileStore.getUsableSpace())
            )));
            strCounterDisk++;
        }
        strJsonString.append(']'); // from Disk(s)
        return strJsonString.toString();
    }

    /**
     * Capturing details about CPU
     * 
     * @return String
     */
    private static String getDetailsAboutCentralPowerUnit() {
        final CentralProcessor processor = hardware.getProcessor();
        final CentralProcessor.ProcessorIdentifier procIdentif = processor.getProcessorIdentifier();
        return Common.getMapIntoJsonString(Map.of(
            "Feature Flags", processor.getFeatureFlags().toString().replace("[", "[\"").replace(", ", "\",\"").replace("]", "\"]"),
            "Family", procIdentif.getFamily(),
            "Identifier", procIdentif.getIdentifier(),
            "Local Processors", processor.getLogicalProcessorCount(),
            "Model", procIdentif.getModel(),
            strName, procIdentif.getName(),
            "Physical", processor.getPhysicalProcessorCount()
        ));
    }

    /**
     * GPU info
     * 
     * @return String
     */
    private static String getDetailsAboutGraphicCards() {
        final StringBuilder strJsonString = new StringBuilder(50);
        final List<GraphicsCard> graphicCards = hardware.getGraphicsCards(); // NOPMD by Daniel Popiniuc on 17.04.2025, 17:39
        int intCounter = 0;
        for (final GraphicsCard  graphicCard : graphicCards) {
            if (intCounter > 0) {
                strJsonString.append(',');
            }
            strJsonString.append(Common.getMapIntoJsonString(Map.of(
                "Device Id", graphicCard.getDeviceId(),
                strName, graphicCard.getName(),
                "Vendor", graphicCard.getVendor(),
                "VRAM", FormatUtil.formatBytes(graphicCard.getVRam()),
                "Driver Version", graphicCard.getVersionInfo()
            )));
            intCounter++;
        }
        return String.format("[%s]", strJsonString);
    }

    /**
     * GPU info
     * 
     * @return String
     */
    private static String getDetailsAboutMonitor() {
        final StringBuilder strJsonString = new StringBuilder();
        final List<Display> displays = hardware.getDisplays(); // NOPMD by Daniel Popiniuc on 17.04.2025, 17:39
        int intCounter = 0;
        for (final Display crtDisplay : displays) {
            if (intCounter > 0) {
                strJsonString.append(',');
            }
            strJsonString.append(digestDisplayDetails(crtDisplay));
            intCounter++;
        }
        return String.format("[%s]", strJsonString);
    }

    /**
     * Operating System details
     * 
     * @return String
     */
    private static String getDetailsAboutOperatingSystem() {
        final OperatingSystem.OSVersionInfo versionInfo = operatingSystem.getVersionInfo();
        return Common.getMapIntoJsonString(Map.of(
            "Architecture", System.getProperty("os.arch"),
            "Build", versionInfo.getBuildNumber(),
            "Code", versionInfo.getCodeName(),
            "Family", operatingSystem.getFamily(),
            "Manufacturer", operatingSystem.getManufacturer(),
            "Name", System.getProperty("os.name"),
            "Platform", SystemInfo.getCurrentPlatform().toString(),
            "Version", versionInfo.getVersion()
        ));
    }

    /**
     * Capturing RAM information
     * 
     * @return String
     */
    private static String getDetailsAboutRandomAccessMemory() {
        final GlobalMemory globalMemory = hardware.getMemory();
        final StringBuilder strJsonString = new StringBuilder();
        strJsonString.append(String.format("{\"Total\":{\"Total\":\"%s\",\"Available\":\"%s\",\"Page Size\":\"%s\"}", FormatUtil.formatBytes(globalMemory.getTotal()), FormatUtil.formatBytes(globalMemory.getAvailable()), FormatUtil.formatBytes(globalMemory.getPageSize())));
        final List<PhysicalMemory> physicalMemories = globalMemory.getPhysicalMemory();
        strJsonString.append(",\"Banks\":[");
        int intCounter = 0;
        for (final PhysicalMemory physicalMemory : physicalMemories) {
            if (intCounter > 0) {
                strJsonString.append(',');
            }
            strJsonString.append(Common.getMapIntoJsonString(Map.of(
                "Bank/Slot Label", physicalMemory.getBankLabel(),
                "Capacity", FormatUtil.formatBytes(physicalMemory.getCapacity()),
                "Clock Speed", FormatUtil.formatHertz(physicalMemory.getClockSpeed()),
                "Manufacturer", physicalMemory.getManufacturer(),
                "Type", physicalMemory.getMemoryType(),
                "Part Number", physicalMemory.getPartNumber().trim(),
                "Serial Number", physicalMemory.getSerialNumber()
            )));
            intCounter++;
        }
        strJsonString.append("]}");
        return strJsonString.toString();
    }

    /**
     * JAVA info
     * 
     * @return String
     */
    private static String getDetailsAboutSoftwarePlatformJava() {
        return Common.getMapIntoJsonString(Map.of(
            "Date", System.getProperty("java.version.date"),
            "Release", System.getProperty("java.vendor.version"),
            "Runtime", System.getProperty("java.runtime.name"),
            "Version", System.getProperty("java.version"),
            "Vendor", System.getProperty("java.vendor"),
            "VM", System.getProperty("java.vm.name")
        ));
    }

    /**
     * JAVA info
     * 
     * @return String
     */
    private static String getDetailsAboutSoftwareUser() {
        return Common.getMapIntoJsonString(Map.of(
            "Country", System.getProperty("user.country"),
            "Country.Format", System.getProperty("user.country.format"),
            "Language", System.getProperty("user.language"),
            "Language.Format", System.getProperty("user.language.format"),
            "Home", System.getProperty("user.home").replace("\\", "\\\\"),
            "Name", System.getProperty("user.name"),
            "Timezone", System.getProperty("user.timezone")
        ));
    }

    /**
     * Constructor
     */
    private EnvironmentCapturingClass() {
        throw new UnsupportedOperationException(Common.strAppClsWrng);
    }
}
