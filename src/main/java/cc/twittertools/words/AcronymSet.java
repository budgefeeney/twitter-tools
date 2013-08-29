package cc.twittertools.words;

import java.util.HashSet;
import java.util.Set;

/**
 * A lookup table of common acronyms exposing a single method: {@link #contains(String)}
 * <p>
 * You can use the default list (via the no-arg constructor) or specify your own list.
 * The lookups are case-insensitive. 
 * @author bryanfeeney
 *
 */
public class AcronymSet
{
	
	private final static Set<String> DEFAULT_ACRONYMS;
	static {
		Set<String> values = new HashSet<>();
		values.add("A/D"); /*Analog To Digital */
		values.add("A/V"); /*Audio/Visual */
		values.add("AAC"); /*Advanced Audio Coding */
		values.add("AARP"); /*AppleTalk Address Resolution Protocol */
		values.add("ABBH"); /*Average Bouncing Busy Hour */
		values.add("ABC"); /*Atanasoff-Berry Computer */
		values.add("ABEND"); /*Abnormal End */
		values.add("AC"); /*Alternating Current */
		values.add("ACD"); /*Automatic Call Distributor */
		values.add("ACES"); /*Asia Cellular Satellite System */
		values.add("ACK"); /*Acknowledge Character */
		values.add("ACL"); /*Access Control List */
		values.add("ACM"); /*Association For Computing Machinery */
		values.add("ACPI"); /*Advanced Configuration And Power Interface */
		values.add("ACTA"); /*America's Carriers Telecommunications Association */
		values.add("ACU"); /*Automatic Calling Unit */
		values.add("ADB"); /*Apple Desktop Bus */
		values.add("ADCCP"); /*Advanced Data Communications Control Protocol */
		values.add("ADO"); /*ActiveX Data Objects */
		values.add("ADP"); /*Automatic Data Processing */
		values.add("ADPCM"); /*Adaptive Differential Pulse Code Modulation */
		values.add("ADS"); /*Alternate Data Streams */
		values.add("ADSL"); /*Asymmetric Digital Subscriber Line */
		values.add("ADVEIS"); /*Anti-Virus Dependent Vulnerabilities In E-mail Infrastructure Security */
		values.add("AES"); /*Asynchronous Event Scheduler */
		values.add("AFAIK"); /*As Far As I Know */
		values.add("AGP"); /*Accelerated Graphics Port */
		values.add("AH"); /*Authentication Header */
		values.add("AI"); /*Artificial Intelligence */
		values.add("AIFF"); /*Audio Interchange File Format */
		values.add("AIFF-C"); /*Audio Interchange File Format */
		values.add("AIML"); /*Astronomical Instrument Markup Language */
		values.add("AIX"); /*Advanced Interactive Executive */
		values.add("ALGOL"); /*Algorithmic Language */
		values.add("ALU"); /*Arithmetic (and) Logic Unit */
		values.add("AMANDA"); /*Advanced Maryland Automatic Network Disk Archiver */
		values.add("AMD"); /*Advanced Micro Devices */
		values.add("AML"); /*Astronomy Markup Language */
		values.add("AMP"); /*Advanced Metal Powder */
		values.add("AMPS"); /*Advanced Mobile Phone System */
		values.add("AMS"); /*American Mobile Satellite Corporation */
		values.add("AN"); /*Active Network */
		values.add("ANSA"); /*Adaptive Network Security Alliance */
		values.add("ANSI"); /*American National Standards Institute */
		values.add("AOL"); /*America Online */
		values.add("APA"); /*All Points Addressable */
		values.add("API"); /*Application Program(ming) Interface */
		values.add("APM"); /*Advanced Power Management */
		values.add("APP"); /*Application Portability Profile */
		values.add("APWG"); /*Anti-Phishing Working Group */
		values.add("ARLL"); /*Advanced Run Length Limited */
		values.add("ARM"); /*Advanced (Acorn) RISC Machine */
		values.add("ARM"); /*Advanced RISC Machines Ltd */
		values.add("ARP"); /*Address Resolution Protocol */
		values.add("ARPA"); /*Advanced Research Projects Agency */
		values.add("ARQ"); /*Automatic Repeat Request */
		values.add("ASAP"); /*Activate Secure Authentication Protocol */
		values.add("ASAP"); /*As Soon As Possible */
		values.add("ASCII"); /*American Standard Code For Information Interchange */
		values.add("ASF"); /*ActiveMovie Streaming Format */
		values.add("ASIC"); /*Application-Specific Integrated Circuit */
		values.add("ASP"); /*Active Server Pages */
		values.add("ASP"); /*Application Service Provider */
		values.add("ASP"); /*Association Of Shareware Professionals */
		values.add("AT"); /*Attention */
		values.add("ATA"); /*AT Attachment */
		values.add("ATL"); /*Active (ActiveX) Template Library */
		values.add("ATM"); /*Asynchronous Transfer Mode */
		values.add("AUI"); /*Attachment Unit Interface */
		values.add("AVI"); /*Audio/Video Interleaved Format */
		values.add("AWHFY"); /*Are We Having Fun Yet */
		values.add("AWT"); /*Abstract Windows Toolkit */
		values.add("B-ISDN"); /*Broadband-Integrated Services Digital Network */
		values.add("B2B"); /*Business-to-Business */
		values.add("BAPI"); /*Business Application Programming Interface */
		values.add("BASIC"); /*Beginners All-purpose Symbolic Instruction Code */
		values.add("BAT"); /*Bandwidth Allocation Technology */
		values.add("BBS"); /*Bulletin Board System */
		values.add("BCC"); /*Blind Carbon Copy */
		values.add("BCC"); /*Block Check Character */
		values.add("BCD"); /*Binary Coded Decimal */
		values.add("BDP"); /*Bandwidth-delay Product */
		values.add("BEDO RAM"); /*Burst EDO RAM */
		values.add("BEOS"); /*Be, Inc. Operating System */
		values.add("BG"); /*Big Grin */
		values.add("BGA"); /*Ball-grid Array */
		values.add("BGP"); /*Border Gateway Protocol */
		values.add("BICMOS"); /*Bipolar Complementary Metal Oxide Semiconductor */
		values.add("BIOS"); /*Basic Input/Output System */
		values.add("BIT"); /*Binary Digit */
		values.add("BITNET"); /*Because Its Time Network */
		values.add("BLOB"); /*Binary Large Object */
		values.add("BNC"); /*Bayonet Neill Concelman */
		values.add("BOOTP"); /*BOOTstrap Protocol */
		values.add("BPEL"); /*Business Process Execution Language */
		values.add("BPL"); /*Bits Per Line */
		values.add("BPP"); /*Bits Per Pixel */
		values.add("BPS"); /*Bits Per Second */
		values.add("BRI"); /*Basic-Rate Interface */
		values.add("BSC"); /*Base Station Controller */
		values.add("BSD"); /*Berkeley Software (System) Distribution (Design) */
		values.add("BSML"); /*Biosequence Markup Language */
		values.add("BSWD"); /*BellSouth Intelligent Wireless Data Network */
		values.add("BTW"); /*By The Way */
		values.add("CA"); /*Certificate Authority */
		values.add("CAD"); /*Computer-Aided Design */
		values.add("CAE"); /*Computer-Aided Engineering */
		values.add("CASE"); /*Computer-Aided Software Engineering */
		values.add("CAV"); /*Constant Angular Velocity */
		values.add("CBT"); /*Computer-Based Training */
		values.add("CCD"); /*Charge-Coupled Device */
		values.add("CCI"); /*Common Content Inspection */
		values.add("CCITT"); /*Consultative Committee For International Telephony And Telegraphy */
		values.add("CD"); /*Carrier Detect */
		values.add("CD-I"); /*Compact Disc-Interactive */
		values.add("CD-PROM"); /*Compact Disc-Programmable Read Only Memory */
		values.add("CD-R"); /*Compact Disc-Recordable */
		values.add("CD-ROM"); /*Compact Disc, Read Only Memory */
		values.add("CD-RW"); /*Compact Disc-Rewritable */
		values.add("CDC"); /*Cult Of The Dead Cow */
		values.add("CDDI"); /*Copper Data Distribution Interface */
		values.add("CDF"); /*Channel Definition Format */
		values.add("CDFS"); /*Compact Disk File System */
		values.add("CDI"); /*Common Data Interface */
		values.add("CDIF"); /*CASE Definition Interchange Facility */
		values.add("CDMA"); /*Code Division Multiple Access */
		values.add("CDPD"); /*Cellular Digital Packet Data */
		values.add("CDRAM"); /*Cached Dynamic Random Access Memory */
		values.add("CERN"); /*Conseil Europeen Pour Le Recherche Nucleaire */
		values.add("CERT"); /*Character Error Rate Testing (European Organization For Nuclear Research) As In, Large Hadron Collider */
		values.add("CERT"); /*The CERT Coordination Center */
		values.add("CFI"); /*Common Flash Interface */
		values.add("CGA"); /*Color Graphics Adapter */
		values.add("CGI"); /*Common Gateway Interface */
		values.add("CGM"); /*Computer Graphics Metafile */
		values.add("CHAP"); /*Challenge Handshake Authentication Protocol */
		values.add("CHRP"); /*Common Hardware Reference Platform */
		values.add("CIDR"); /*Classless Inter-Domain Routing */
		values.add("CIE"); /*Color Model = Commission Internationale De L'Eclairage Color Model */
		values.add("CIFF"); /*Camera Image File Format */
		values.add("CIR"); /*Committed Information Rate */
		values.add("CISC"); /*Complex Instruction-Set Computing */
		values.add("CISC"); /*Creative Integrated Solutions Consortium, LLC */
		values.add("CIX"); /*Commercial Internet Exchange Association */
		values.add("CLEC"); /*Competitive Local Exchange Carriers */
		values.add("CLV"); /*Constant Linear Velocity */
		values.add("CME"); /*Common Malware Enumeration */
		values.add("CMIP"); /*Common Management Information Protocol */
		values.add("CML"); /*Chemical Markup Language */
		values.add("CMMI"); /*Capability Maturity Model Integration */
		values.add("CMOS"); /*Complementary Metal-Oxide Semiconductor */
		values.add("CMS"); /*Color Management System */
		values.add("CMS"); /*Content Management System */
		values.add("CMTS"); /*Cable Modem Termination System */
		values.add("CMYK"); /*Cyan Magenta Yellow Black (Key) */
		values.add("CNG"); /*Calling Tone */
		values.add("COBOL"); /*Common Business Oriented Language */
		values.add("CODASYL"); /*Conference On Data Systems Languages */
		values.add("CODEC"); /*Compression/Decompression */
		values.add("COM"); /*Component Object Model */
		values.add("COMDEX"); /*Communications And Data Processing Exposition */
		values.add("CORBA"); /*Common Object Request Broker Architecture */
		values.add("CP/M"); /*Control Program For Microprocessors (Control Program/Monitor) */
		values.add("CPE"); /*Customer Premises Equipment */
		values.add("CPS"); /*Characters Per Second */
		values.add("CPU"); /*Central Processing Unit */
		values.add("CR"); /*Carriage Return */
		values.add("CRAM"); /*Card RAM */
		values.add("CRAM"); /*Computational RAM */
		values.add("CRC"); /*Cyclic Redundancy Check (Code) */
		values.add("CRM"); /*Customer Relationship Management */
		values.add("CRT"); /*Cathode-Ray Tube */
		values.add("CSDGM"); /*Content Standard For Digital Geospatial Metadata */
		values.add("CSMA/CA"); /*Carrier Sense Multiple Access/Collision Avoidance */
		values.add("CSMA/CD"); /*Carrier Sense Multiple Access/Collision Detection */
		values.add("CSS"); /*Cascading Style Sheets */
		values.add("CSTN"); /*Color Supertwist Nematic */
		values.add("CSU"); /*Channel Service Unit */
		values.add("CSV"); /*Comma-Separated Value / Variable */
		values.add("CTI"); /*Computer-Telephony Integration */
		values.add("CTP"); /*Community Technical Preview */
		values.add("CTR"); /*Click-Through Ratio */
		values.add("CUA"); /*Common User Access */
		values.add("CWM"); /*Common Warehouse Metadata */
		values.add("CWR"); /*Congestion Window Reduced */
		values.add("CYA"); /*Cover Your Ass */
		values.add("CYBORG"); /*Cybernetic Organism */
		values.add("CYMK"); /*Cyan Yellow Magenta Black (Key) */
		values.add("DAC"); /*Digital-to-Analog Converter */
		values.add("DAFS"); /*Direct Access File System */
		values.add("DAFS"); /*Document Attribute Format Specification */
		values.add("DAO"); /*Data Access Objects */
		values.add("DAO"); /*Disk At Once */
		values.add("DASD"); /*Direct Access Storage Device */
		values.add("DAT"); /*Digital Audio Tape */
		values.add("DB"); /*Database */
		values.add("DB2"); /*Database 2 */
		values.add("DBMS"); /*Database Management System */
		values.add("DCC"); /*Direct Cable Connection */
		values.add("DCE"); /*Data Communications Equipment */
		values.add("DCF"); /*Design Rule For Camera File Systems */
		values.add("DCI"); /*Display Control Interface */
		values.add("DDE"); /*Dynamic Data Exchange */
		values.add("DDI"); /*Direct Draw Interface */
		values.add("DDOS"); /*Distributed Denial-of-Service */
		values.add("DDR SDRAM"); /*Double Data Rate-Synchronous DRAM */
		values.add("DDS"); /*Digital Data Storage */
		values.add("DEBI"); /*DMA Extended Bus Interface */
		values.add("DEC"); /*Digital Equipment Corporation */
		values.add("DES"); /*Data Encryption Standard */
		values.add("DFM"); /*Design For Manufacturing */
		values.add("DHCP"); /*Dynamic Host Configuration Protocol */
		values.add("DID"); /*Direct Inward Dialing */
		values.add("DIF"); /*Data Interchange Format */
		values.add("DIF"); /*Directory Interchange Format */
		values.add("DIMM"); /*Dual In-Line Memory Module */
		values.add("DIN"); /*Deutsches Institut F?r Normung (German Institute For Standardization) */
		values.add("DINOR"); /*Divided Bit-line NOR */
		values.add("DIP"); /*Dual In-line Package */
		values.add("DIVX"); /*Digital Video Express */
		values.add("DIZ"); /*Description In ZIP */
		values.add("DLC"); /*Data Link Control */
		values.add("DLL"); /*Dynamic Link Library */
		values.add("DLP"); /*Digital Light Processing */
		values.add("DLT"); /*Digital Linear Tape */
		values.add("DMA"); /*Direct Memory Access */
		values.add("DMCA"); /*Digital Millennium Copyright Act */
		values.add("DMG"); /*Data Mining Group */
		values.add("DMI"); /*Desktop Management Interface */
		values.add("DNS"); /*Domain Name Service (System) */
		values.add("DOCSIS/MCNS"); /*Data Over Cable Services And Interface Specifications For Multimedia Cable Network Systems */
		values.add("DOM"); /*Document Object Model */
		values.add("DOS"); /*Denial-of-Service */
		values.add("DOS"); /*Disk Operating System */
		values.add("DPI"); /*Dots Per Inch */
		values.add("DPMI"); /*DOS Protected Mode Interface */
		values.add("DRAM"); /*Dynamic Random Access Memory */
		values.add("DSL"); /*Digital Subscriber Line */
		values.add("DSLAM"); /*Digital Subscriber Line Access Multiplexer (Module) */
		values.add("DSOM"); /*Distributed System Object Model */
		values.add("DSP"); /*Digital Signal Processor */
		values.add("DSR"); /*Data Set Ready */
		values.add("DSTN"); /*Double-layer Supertwist Nematic. (in Passive-matrix LCD Technology) */
		values.add("DSU"); /*Data Service Unit */
		values.add("DSVD"); /*Digital Simultaneous Voice And Data */
		values.add("DTD"); /*Document Type Definition */
		values.add("DTE"); /*Data Termination (Terminal) Equipment */
		values.add("DUN"); /*Dial-Up Networking */
		values.add("DVD"); /*Digital Versatile (Video) Disc */
		values.add("DVD+RW"); /*Digital Versatile (Video) Disc + Read/Write */
		values.add("DVD-RAM"); /*Digital Versatile (Video) Disc - Random Access Memory */
		values.add("DVD-ROM"); /*Digital Versatile (Video) Disc - Read Only Memory */
		values.add("DVI"); /*Digital Video Interactive */
		values.add("DWDM"); /*Dense Wave Division Multiplexing */
		values.add("DXF"); /*Data Exchange File (Format) */
		values.add("EAP"); /*Extensible Authentication Protocol */
		values.add("EBCDIC"); /*Extended Binary Coded Decimal Interchange Code */
		values.add("ECC"); /*Error Correction Code (Error Checking And Correction) */
		values.add("ECN"); /*Explicit Congestion Notification */
		values.add("EDGE"); /*Enhanced Data Rates For Global Evolution */
		values.add("EDI"); /*Electronic Data Interchange */
		values.add("EDLC"); /*Electrochemical Double-layer Capacitor */
		values.add("EDO RAM"); /*Extended Data-Out Random Access Memory */
		values.add("EDP"); /*Electronic Data Processing */
		values.add("EDRAM"); /*Enhanced Dynamic Random Access Memory */
		values.add("EEMS"); /*Enhanced Expanded Memory Specification */
		values.add("EEPROM"); /*Electrically Erasable Programmable Read-Only Memory */
		values.add("EFT"); /*Electronic Funds Transfer */
		values.add("EGA"); /*Enhanced Graphics Adapter */
		values.add("EIA"); /*Electronic Industries Alliance (Association) */
		values.add("EIDE"); /*Enhanced Integrated Device Electronics */
		values.add("EIDE"); /*Enhanced Intelligent Drive Electronics */
		values.add("EISA"); /*Extended Industry Standard Architecture */
		values.add("ELD"); /*Electroluminescent Display */
		values.add("ELF"); /*Extremely Low Frequency */
		values.add("EMM"); /*Expanded Memory Manager */
		values.add("EMS"); /*Expanded Memory Specification */
		values.add("EOF"); /*End Of File */
		values.add("EPP"); /*Enhanced Parallel Port */
		values.add("EPROM"); /*Erasable Programmable Read-Only Memory */
		values.add("EPS"); /*Encapsulated PostScript */
		values.add("ERP"); /*Enterprise Resource Planning */
		values.add("ESC"); /*Escape Character */
		values.add("ESCD"); /*Extended System Configuration Data */
		values.add("ESDI"); /*Enhanced System Device Interface */
		values.add("ESP"); /*Encapsulating Security Payload */
		values.add("ESP"); /*Eudora Sharing Protocol */
		values.add("ETOX"); /*EPROM Tunnel Oxide */
		values.add("EULA"); /*End User License Agreement */
		values.add("EWAN"); /*Emulator Without A Name */
		values.add("EXIF"); /*Exchangeable Image File */
		values.add("FAHQT"); /*Fully Automatic High-Quality Translation */
		values.add("FAMOS"); /*Floating Gate Avalanche MOS */
		values.add("FAQ"); /*Frequently Asked Questions */
		values.add("FAT"); /*File Allocation Table */
		values.add("FBGA"); /*Fine-pitch Ball-grid Array */
		values.add("FCB"); /*File Control Block */
		values.add("FCC"); /*File Carbon Copy */
		values.add("FDD"); /*Floppy Disk Drive */
		values.add("FDDI"); /*Fiber Distributed Data Interface */
		values.add("FDHD"); /*Floppy Drive, High Density */
		values.add("FDM"); /*Frequency Division Multiplexing */
		values.add("FF"); /*Form Feed */
		values.add("FFS"); /*Flash File System */
		values.add("FIF"); /*Fractal Image Format */
		values.add("FIFO"); /*First In, First Out */
		values.add("FLOPS"); /*Floating-point Operations Per Second */
		values.add("FORTRAN"); /*Formula Translator; Programming Language */
		values.add("FPM RAM"); /*Fast Page Mode RAM */
		values.add("FPU"); /*Floating-Point Unit */
		values.add("FR"); /*Frame Relay */
		values.add("FRAD"); /*Frame Relay Assembler/Disassembler */
		values.add("FRAM"); /*Ferroelectric RAM */
		values.add("FSK"); /*Frequency Shift Keying */
		values.add("FSP"); /*File Service Protocol */
		values.add("FTP"); /*File Transfer Protocol */
		values.add("FWIW"); /*For What It's Worth */
		values.add("FYI"); /*For Your Information */
		values.add("GBE"); /*Gigabit Ethernet */
		values.add("GDI"); /*Graphics Device Interface */
		values.add("GIF"); /*Graphical Interchange Format */
		values.add("GIGO"); /*Garbage In, Garbage Out */
		values.add("GIMP"); /*General (GNU) Image Manipulation Program */
		values.add("GIS"); /*Geographic Information System */
		values.add("GM"); /*General MIDI */
		values.add("GMT"); /*Greenwich Mean Time */
		values.add("GNU"); /*Recursive Acronym For ?GNU's Not Unix? */
		values.add("GPF"); /*General Protection Fault */
		values.add("GPRS"); /*General Packet Radio Service */
		values.add("GPS"); /*Global Positioning Satellite (System) */
		values.add("GREP"); /*Globally Search For The Regular Expression And Print */
		values.add("GREP"); /*Generalized Regular Expression Pattern-matcher */
		values.add("GRINS"); /*Graphical Interface To SMIL */
		values.add("GSM"); /*Global System For Mobiles */
		values.add("GUI"); /*Graphical User Interface */
		values.add("GUID"); /*Globally Unique Identifier */
		values.add("HAL"); /*Hardware Abstraction Layer */
		values.add("HD"); /*High Density */
		values.add("HD"); /*Hard Drive */
		values.add("HDD"); /*Hard Disk Drive */
		values.add("HDLC"); /*High-level Data Link Control */
		values.add("HDML"); /*Handheld Device Markup Language */
		values.add("HDR"); /*High Data Rate */
		values.add("HDTV"); /*High-Definition Television */
		values.add("HFS"); /*Hierarchical Filing System */
		values.add("HMA"); /*High Memory Area */
		values.add("HMD"); /*Head-Mounted Display */
		values.add("HP"); /*Hewlett-Packard */
		values.add("HP-GL"); /*Hewlett-Packard Graphics Language */
		values.add("HPAS"); /*Hypermedia Presentation And Authoring System */
		values.add("HPFS"); /*High Performance File System */
		values.add("HSL"); /*Hypermedia Synchronization Language */
		values.add("HSM"); /*Hierarchical Storage Management */
		values.add("HTML"); /*Hypertext Markup Language */
		values.add("HTTP"); /*Hypertext Transfer Protocol */
		values.add("HTTPD"); /*Hypertext Transfer Protocol Daemon (Server) */
		values.add("I/O"); /*Input/Output */
		values.add("IAB"); /*Internet Architecture Board */
		values.add("IAC"); /*Internet Access Coalition */
		values.add("IAD"); /*Integrated Access Device */
		values.add("IAHC"); /*Internet International Ad Hoc Committee */
		values.add("IANA"); /*Internet Assigned Numbers Authority */
		values.add("IANAL"); /*I Am Not A Lawyer */
		values.add("IAS"); /*Internet Authentication Service */
		values.add("IBM"); /*International Business Machines */
		values.add("ICA"); /*Independent Computing Architecture */
		values.add("ICANN"); /*The Internet Corporation For Assigned Names And Numbers */
		values.add("ICMP"); /*Internet Control Message Protocol */
		values.add("IDE"); /*Intelligent (Integrated) Drive Electronics */
		values.add("IDEA"); /*International Data Encryption Algorithm */
		values.add("IDSL"); /*ISDN Digital Subscriber Line */
		values.add("IEEE"); /*Institute Of Electrical And Electronics Engineers */
		values.add("IETF"); /*Internet Engineering Task Force */
		values.add("IGES"); /*Initial Graphics Exchange Specification */
		values.add("IGMP"); /*Internet Group Management Protocol */
		values.add("IIM"); /*Internet Interaction Management */
		values.add("IIOP"); /*Internet Inter-ORB Protocol */
		values.add("IIS"); /*Internet Information Server */
		values.add("IMAP"); /*Internet Message Access Protocol */
		values.add("IMHO"); /*In My Humble Opinion */
		values.add("INTERNIC"); /*Internet Network Information Center */
		values.add("IOS"); /*Input Output System */
		values.add("IP"); /*Internet Protocol */
		values.add("IPSEC"); /*Internet Protocol Security */
		values.add("IPTV"); /*Internet Protocol Television */
		values.add("IPX"); /*Internetwork Packet Exchange */
		values.add("IRC"); /*Internet Relay Chat */
		values.add("IRDA"); /*Infrared Data Association */
		values.add("IRQ"); /*Interrupt Request */
		values.add("ISA"); /*Industry Standard Architecture */
		values.add("ISAM"); /*Indexed Sequential Access Method */
		values.add("ISAPI"); /*Internet Server API */
		values.add("ISDN"); /*Integrated Services Digital Network */
		values.add("ISO"); /*International Organization For Standardization */
		values.add("ISP"); /*Internet Service Provider */
		values.add("ITU"); /*International Telecommunications Union */
		values.add("IX"); /*Internet Exchange */
		values.add("JBIG"); /*Joint Bi-level Image Experts Group */
		values.add("JCL"); /*Job Control Language */
		values.add("JDBC"); /*Java Database Connectivity */
		values.add("JDK"); /*Java Developers (Development) Kit */
		values.add("JEIDA"); /*Japan Electronic Industry Development Association */
		values.add("JFIF"); /*JPEG File Interchange Format */
		values.add("JPEG"); /*Joint Photographic Experts Group */
		values.add("KB"); /*Kilobit */
		values.add("KB"); /*Kilobyte */
		values.add("KBPS"); /*Kilobits Per Second */
		values.add("L2TP"); /*Layer Two Tunneling Protocol */
		values.add("LAN"); /*Local Area Network */
		values.add("LAWN"); /*Local-Area Wireless Network */
		values.add("LBA"); /*Logical Block Addressing */
		values.add("LBV"); /*Layered Biometric Verification */
		values.add("LCD"); /*Liquid-Crystal Display */
		values.add("LDAP"); /*Lightweight Directory Access Protocol */
		values.add("LED"); /*Light Emitting Diode */
		values.add("LEP"); /*Light Emitting Polymers */
		values.add("LGMR"); /*Laser Guided Magnetic Recording */
		values.add("LI-ION"); /*Lithium Ion */
		values.add("LIFO"); /*Last In, First Out */
		values.add("LILO"); /*Linux Loader */
		values.add("LIM"); /*Lifeline Interface Module */
		values.add("LIM"); /*Lotus-Intel-Microsoft */
		values.add("LMB"); /*Left Mouse Button */
		values.add("LPM"); /*Lifeline Port Module */
		values.add("LPM"); /*Lines Per Minute */
		values.add("LPT"); /*Local Printer Terminal */
		values.add("LTO"); /*Linear Tape Open */
		values.add("LZW"); /*Lempel-Zif-Welsh */
		values.add("MAE"); /*Metropolitan Area Exchange (Ethernet) */
		values.add("MAN"); /*Metropolitan Area Network */
		values.add("MAPI"); /*Messaging Application Programming Interface */
		values.add("MARS"); /*Multi-player Animated Role-playing System */
		values.add("MATHML"); /*Mathematical Markup Language */
		values.add("MAU"); /*Media Access Unit */
		values.add("MAU"); /*Multistation Access Unit */
		values.add("MB"); /*Megabit */
		values.add("MB"); /*Megabyte */
		values.add("MBPS"); /*Megabits Per Second */
		values.add("MBR"); /*Master Boot Record */
		values.add("MCA"); /*Micro Channel Architecture */
		values.add("MCGA"); /*Multi-Color Graphics Array */
		values.add("MCI"); /*Media Control Interface */
		values.add("MDA"); /*Monochrome Display Adapter */
		values.add("MDC"); /*Metadata Coalition */
		values.add("MDI"); /*Multiple Document Interface */
		values.add("MDIS"); /*Metadata Interchange Specification */
		values.add("MEMS"); /*MicroElectro-Mechanical System */
		values.add("MFLOPS"); /*Millions Of Floating Point Operations Per Second */
		values.add("MFM"); /*Modified Frequency Modulation */
		values.add("MIB"); /*Management Information Base */
		values.add("MICR"); /*Magnetic Ink Character Recognition (Reader) */
		values.add("MIDI"); /*Musical Instrument Digital Interface */
		values.add("MIF"); /*Management Information Format */
		values.add("MILNET"); /*Military Network */
		values.add("MIME"); /*Multipurpose Internet Mail Extension */
		values.add("MIPS"); /*Millions Of Instructions Per Second */
		values.add("MIS"); /*Management Information System */
		values.add("MIS"); /*Management Information Services */
		values.add("MLC"); /*Multilevel Cell */
		values.add("MMDS"); /*Multichannel Multipoint Distribution System */
		values.add("MMU"); /*Memory Management Unit */
		values.add("MMX"); /*Matrix Math Extensions (Multimedia Extensions) */
		values.add("MNG"); /*Multiple-image Network Graphics */
		values.add("MNP"); /*Microcom Networking Protocol */
		values.add("MODEM"); /*Modulator/Demodulator */
		values.add("MOF"); /*Meta Object Facility */
		values.add("MOO"); /*Mud, Object Oriented */
		values.add("MOS"); /*Metal Oxide Semiconductor */
		values.add("MOSFET"); /*Metal Oxide Semiconductor Field Effect Transistor */
		values.add("MOSIS"); /*MOS Implementation System */
		values.add("MOSS"); /*Mime Object Security Services */
		values.add("MP3"); /*MPEG Layer 3 */
		values.add("MPC"); /*Matchbox PC */
		values.add("MPC"); /*Multimedia Personal Computer */
		values.add("MPEG"); /*Motion Picture Experts Group */
		values.add("MPI"); /*Message Passing Interface */
		values.add("MRC"); /*Magneto-Resistive Cluster */
		values.add("MS-DOS"); /*Microsoft Disk Operating System */
		values.add("MSN"); /*Microsoft Network */
		values.add("MTA"); /*Media Terminal Adapter */
		values.add("MTBF"); /*Mean Time Between Failure */
		values.add("MTU"); /*Maximum Transmission Unit */
		values.add("MUD"); /*Multiple User (or Multi-User) Dungeon (Dimension) */
		values.add("MUSH"); /*Multi-User Shared Hallucination */
		values.add("MVS"); /*Multiple Virtual Storage */
		values.add("NAP"); /*Network Access Point */
		values.add("NAS"); /*Network-Attached Storage */
		values.add("NAT"); /*Network Address Translation */
		values.add("NCSA"); /*National Center For Supercomputing Applications */
		values.add("NDIS"); /*Network Driver Interface Specification */
		values.add("NDMP"); /*Network Data Management Protocol */
		values.add("NDS"); /*Novell Directory Services */
		values.add("NETBEUI"); /*Network BIOS Extended User Interface */
		values.add("NETBIOS"); /*Network Basic Input Output System */
		values.add("NFS"); /*Network File System */
		values.add("NGI"); /*Next Generation Internet */
		values.add("NIC"); /*Network Interface Card */
		values.add("NIMH"); /*Nickel Metal Hydride */
		values.add("NNTP"); /*Network News Transfer Protocol */
		values.add("NOP"); /*No Operation */
		values.add("NOS"); /*Network Operating System */
		values.add("NSAPI"); /*Netscape Server API */
		values.add("NSF"); /*National Science Foundation */
		values.add("NSFNET"); /*National Science Foundation Network */
		values.add("NSP"); /*Network Service Provider */
		values.add("NTFS"); /*NT File System */
		values.add("NTSC"); /*National Television Standards Committee */
		values.add("NUMA"); /*Non-Uniform Memory Access */
		values.add("NURBS"); /*Non-Uniform Rational B-Spline */
		values.add("NVRAM"); /*Non-Volatile RAM */
		values.add("OC"); /*Optical Carrier */
		values.add("OCR"); /*Optical Character Recognition */
		values.add("OCX"); /*OLE Custom Control */
		values.add("ODBC"); /*Open Database Connectivity */
		values.add("ODI"); /*Open Data-link Interface */
		values.add("OEM"); /*Original Equipment Manufacturer */
		values.add("OIM"); /*Open Information Model */
		values.add("OLAP"); /*Online Analytical Processing */
		values.add("OLE"); /*Object Linking And Embedding */
		values.add("OLED"); /*Organic Light-Emitting Diode */
		values.add("OLTP"); /*On-Line Transaction Processing */
		values.add("OMG"); /*Object Management Group */
		values.add("OMG"); /*Oh My God */
		values.add("OOP"); /*Object-Oriented Programming */
		values.add("OPENSRS"); /*Open Shared Registration System */
		values.add("OPSEC"); /*Open Platform For Secure Enterprise Connectivity Alliance */
		values.add("OSF"); /*Open Software Foundation */
		values.add("OSI"); /*Open System Interconnection */
		values.add("OSPF"); /*Open Shortest Path First */
		values.add("OSR"); /*OEM System Release */
		values.add("OTOH"); /*On The Other Hand */
		values.add("P-SRAM"); /*Pseudo-Static RAM */
		values.add("P3P"); /*Platform For Privacy Preferences */
		values.add("PAD"); /*Portable Application Description */
		values.add("PAL"); /*Phase Alternating Line */
		values.add("PAL"); /*Programmable Array Logic */
		values.add("PAN"); /*Personal Area Network */
		values.add("PAP"); /*Password Authentication Protocol */
		values.add("PB SRAM"); /*Pipelined Burst Static RAM */
		values.add("PBX"); /*Private Branch Exchange */
		values.add("PC"); /*Personal Computer */
		values.add("PC/TV"); /*Personal Computer And Television */
		values.add("PCB"); /*Printed Circuit Board */
		values.add("PCI"); /*Peripheral Component Interface */
		values.add("PCL"); /*Printer Control Language */
		values.add("PCM"); /*Pulse Code Modulation */
		values.add("PCMCIA"); /*Personal Computer Memory Card International Association */
		values.add("PCS"); /*Personal Communications System */
		values.add("PDA"); /*Personal Digital Assistant */
		values.add("PDC"); /*Primary Domain Controller */
		values.add("PDF"); /*Portable Document Format */
		values.add("PDL"); /*Page Description Language */
		values.add("PE"); /*Portable Executable */
		values.add("PERL"); /*Practical Extraction And Report Language */
		values.add("PFR"); /*Portable Font Resource */
		values.add("PGA"); /*Pad Grid Array */
		values.add("PGA"); /*Pin Grid Array */
		values.add("PGA"); /*Professional Graphic Adapter */
		values.add("PGM"); /*Pragmatic General Multicast / Pretty Good Multicast */
		values.add("PGP"); /*Pretty Good Privacy */
		values.add("PHB"); /*Photochemical Hole Burning */
		values.add("PHP"); /*PHP Hypertext Preprocessor (Personal Home Page) */
		values.add("PIC"); /*Programmable Interrupt Controller */
		values.add("PICNIC"); /*Problem In Chair, Not In Computer */
		values.add("PIF"); /*Program Information File */
		values.add("PIM"); /*Personal Information Manager */
		values.add("PIN"); /*Personal Identification Number */
		values.add("PING"); /*Packet Internet Groper */
		values.add("PIO"); /*Programmed Input Output */
		values.add("PIP"); /*Peripheral Interchange Program */
		values.add("PISO"); /*Parallel-In/Serial-Out */
		values.add("PITA"); /*Pain In The Ass / Arse */
		values.add("PJTF"); /*Portable Job Ticket Format */
		values.add("PKI"); /*Public Key Infrastructure */
		values.add("PLA"); /*Programmable Logic Array */
		values.add("PLD"); /*Programmable Logic Device */
		values.add("PMML"); /*Predictive Modeling Markup Language */
		values.add("PMS"); /*Pantone Matching System */
		values.add("PMT"); /*Photomultiplier Tube */
		values.add("PNG"); /*Portable Network Graphics; Image Format */
		values.add("PNP"); /*Plug And Play */
		values.add("POP"); /*Post Office Protocol */
		values.add("POS"); /*Pivoting Optical Servo */
		values.add("POSIX"); /*Portable Operating System Interface For UNIX */
		values.add("POST"); /*Power On Self Test */
		values.add("POTS"); /*Plain Old Telephone Service */
		values.add("PPCP"); /*PowerPC Platform */
		values.add("PPM"); /*Pages Per Minute */
		values.add("PPP"); /*Point-to-Point Protocol */
		values.add("PPPTP"); /*Point-to-Point Tunneling Protocol */
		values.add("PQDIF"); /*Power Quality Data Interchange Format */
		values.add("PRAM"); /*Parameter RAM */
		values.add("PRI"); /*Primary-Rate Interface */
		values.add("PRL"); /*Packetized Residential Lifeline */
		values.add("PROM"); /*Programmable Read-Only Memory */
		values.add("PSTN"); /*Public Switched Telephone Network */
		values.add("PTH"); /*Plated Through-Hole */
		values.add("PUP"); /*Potentially Unwanted Program */
		values.add("PURL"); /*Persistent URL */
		values.add("PVC"); /*Permanent Virtual Circuit */
		values.add("PWB"); /*Printed Wire Board */
		values.add("QBE"); /*Query By Example */
		values.add("QCELP"); /*QualComm Code Excited Linear Predictive Coding */
		values.add("QCIF"); /*Quarter Common Intermediate Format */
		values.add("QIC"); /*Quarter-Inch Cartridge */
		values.add("QOS"); /*Quality Of Service */
		values.add("QSIG"); /*Q (point Of The ISDN Model) Signaling */
		values.add("RADIUS"); /*Remote Authentication Dial-In User Service */
		values.add("RADSL"); /*Rate-Adaptive Digital Subscriber Line */
		values.add("RAID"); /*Redundant Array Of Independent Disks */
		values.add("RAID"); /*Redundant Array Of Inexpensive Disks */
		values.add("RAM"); /*Random Access Memory */
		values.add("RAMDAC"); /*Random Access Memory Digital-to-Analog Converter */
		values.add("RARP"); /*Reverse Address Resolution Protocol */
		values.add("RAS"); /*Remote Access Services */
		values.add("RC4"); /*RC4 Stream Cipher (Rivest Cipher 4) */
		values.add("RDBMS"); /*Relational Database Management System */
		values.add("RDF"); /*Resource Description Framework */
		values.add("RDRAM"); /*Rambus DRAM */
		values.add("RFC"); /*Request For Comments */
		values.add("RFI"); /*Radio Frequency Interference */
		values.add("RFID"); /*Radio Frequency Identification */
		values.add("RFP"); /*Request For Proposal */
		values.add("RGB"); /*Red Green Blue */
		values.add("RIP"); /*Raster Image Processor */
		values.add("RIP"); /*Regulation Of Investigatory Powers Act */
		values.add("RISC"); /*Reduced Instruction-Set Computing */
		values.add("RJ-11"); /*Registered Jack-11 */
		values.add("RJ-45"); /*Registered Jack-45 */
		values.add("RLL"); /*Run Length Limited */
		values.add("RMB"); /*Right Mouse Button */
		values.add("RMON"); /*Remote Monitoring */
		values.add("RMTP"); /*Reliable Multicast Transport Protocol */
		values.add("ROFL"); /*Rolling On Floor, Laughing */
		values.add("ROM"); /*Read-Only Memory */
		values.add("RPG"); /*Report Program Generator */
		values.add("RPG"); /*Role-Playing Game */
		values.add("RPM"); /*Revolutions Per Minute */
		values.add("RSN"); /*Real Soon Now */
		values.add("RSVP"); /*Resource Reservation Setup Protocol */
		values.add("RTF"); /*Rich Text Format */
		values.add("RTFM"); /*Read The F*cking Manual */
		values.add("RTSP"); /*Realtime Streaming Protocol */
		values.add("RTT"); /*Round-trip Time */
		values.add("S-HTTP"); /*Secure HTTP */
		values.add("S/MIME"); /*Secure/MIME */
		values.add("SAA"); /*System Application Architecture */
		values.add("SAN"); /*Storage Area Network */
		values.add("SAP"); /*SAP America, Inc. (SAP AG) */
		values.add("SAP"); /*Secondary Audio Program */
		values.add("SAP"); /*Service Advertising Protocol */
		values.add("SAS"); /*Serial Attached SCSI */
		values.add("SBB"); /*Storage Bridge Bay Working Group */
		values.add("SCAM"); /*SCSI Configuration Automatically */
		values.add("SCART"); /*Syndicat Des Constructeurs D'Appareils Radior?cepteurs Et T?l?viseurs (Radio And Television Receiver Manufacturer's Association); 21-pin Connector Standard For AV (Audio-visual) Equipment */
		values.add("SCSI"); /*Small Computer System Interface */
		values.add("SDH"); /*Synchronous Digital Hierarchy */
		values.add("SDK"); /*Software Development Kit */
		values.add("SDRAM"); /*Synchronous Dynamic RAM */
		values.add("SDSL"); /*Symmetric Digital Subscriber Line */
		values.add("SET"); /*Secure Electronic Transaction */
		values.add("SFF"); /*Small Form-Factor */
		values.add("SGI"); /*Silicon Graphics Incorporated */
		values.add("SGML"); /*Standard Generalized Markup Language */
		values.add("SGRAM"); /*Synchronous Graphic RAM */
		values.add("SIG"); /*Special Interest Group */
		values.add("SIMD"); /*Single Instruction Multiple Data */
		values.add("SIMM"); /*Single In-line Memory Module */
		values.add("SIP"); /*Single In-line Package */
		values.add("SLDRAM"); /*SyncLink DRAM */
		values.add("SLED"); /*Single Large Expensive Disk */
		values.add("SLIP"); /*Serial Line Internet Protocol */
		values.add("SMART"); /*Self-Monitoring, Analysis And Reporting Technology */
		values.add("SMB"); /*Server Message Block */
		values.add("SMDS"); /*Switched Multimegabit Data Services */
		values.add("SMIF"); /*Stream-based Model Interchange Format */
		values.add("SMIL"); /*Synchronized Multimedia Integration Language */
		values.add("SMP"); /*Simple Management Protocol */
		values.add("SMP"); /*Symmetric Multiprocessing */
		values.add("SMS"); /*Systems Management Server */
		values.add("SMTP"); /*Simple Mail Transfer Protocol */
		values.add("SNA"); /*Systems Network Architecture */
		values.add("SNMP"); /*Simple Network Management Protocol */
		values.add("SOAP"); /*Simple Object Access Protocol */
		values.add("SOC"); /*System-on-a-Chip */
		values.add("SOHO"); /*Small Office/Home Office */
		values.add("SONET"); /*Synchronous Optical Network */
		values.add("SPARC"); /*Scalable Processor Architecture */
		values.add("SPE"); /*Superparamagnetic Effect */
		values.add("SPEC"); /*Standard Performance Evaluation Corporation */
		values.add("SPID"); /*Service Profile (Provider) Identification */
		values.add("SPIM"); /*Spam Over Instant Messaging */
		values.add("SPX"); /*Sequenced Packet Exchange */
		values.add("SQL"); /*Structured Query Language */
		values.add("SRAM"); /*Static Random Access Memory */
		values.add("SSD"); /*Solid-state Disk */
		values.add("SSI"); /*Server-Side Include */
		values.add("SSL"); /*Secure Sockets Layer */
		values.add("SSSCA"); /*Security Systems Standards And Certification Act */
		values.add("ST506/412"); /*Seagate Technologies 506 And 412 Controller (older Technology) */
		values.add("SVC"); /*Switched Virtual Circuit */
		values.add("SVG"); /*Scalable Vector Graphics */
		values.add("SVGA"); /*Super Video Graphics Array */
		values.add("SWISH"); /*Simple Web Indexing System For Humans */
		values.add("SYN"); /*Synchronize Sequence Number */
		values.add("SYSOP"); /*System Operator */
		values.add("T1"); /*T-One - Carrier Signal (communications) */
		values.add("T3"); /*T-Three - Carrier Signal (communications) */
		values.add("TA"); /*Terminal Adapter */
		values.add("TANSTAAFL"); /*There Ain't No Such Thing As A Free Lunch */
		values.add("TAPI"); /*Telephony Application Programming Interface */
		values.add("TCP/IP"); /*Transmission / Transport Control Protocol/Internet Protocol */
		values.add("TDM"); /*Time Division Multiplexing */
		values.add("TDMA"); /*Time Division Multiple Access */
		values.add("TFT"); /*Thin Film Transistor */
		values.add("TFTP"); /*Trivial File Transfer Protocol */
		values.add("TIA"); /*Thanks In Advance */
		values.add("TIFF"); /*Tagged Image File Format */
		values.add("TIGA"); /*Texas Instruments Graphics Architecture */
		values.add("TLD"); /*Top-Level Domain */
		values.add("TOPS"); /*Transparent Operating System */
		values.add("TPI"); /*Tracks Per Inch */
		values.add("TRAM"); /*Transactional RAM */
		values.add("TSAPI"); /*Telephony Service API */
		values.add("TSR"); /*Terminate-and-Stay-Resident */
		values.add("TTF"); /*True Type Font */
		values.add("TTL"); /*Transistor-Transistor Logic */
		values.add("TWAIN"); /*Technology Without An Interesting Name */
		values.add("TWIP"); /*Twentieth Of A Point */
		values.add("UAC"); /*User Account Control */
		values.add("UART"); /*Universal Asynchronous Receiver/Transmitter */
		values.add("UCITA"); /*Uniform Computer Information Transaction Act */
		values.add("UDDI"); /*Universal Description, Discovery And Integration */
		values.add("UDP"); /*UDP - User Datagram Protocol */
		values.add("UIN"); /*Universal Internet Number */
		values.add("ULSI"); /*Ultra Large Scale Integration */
		values.add("ULTRAATA"); /*Ultra AT Attachment */
		values.add("ULTRADMA"); /*Ultra Direct Memory Access */
		values.add("UMB"); /*Upper Memory Block */
		values.add("UML"); /*Unified Modeling Language */
		values.add("UNC"); /*Universal Naming Convention */
		values.add("UNIX"); /*Universal Interactive Executive / Uniplexed Information And Computing System */
		values.add("UPS"); /*Uninterruptible Power Supply */
		values.add("URI"); /*Uniform Resource Indicator */
		values.add("URL"); /*Uniform Resource Locator */
		values.add("USB"); /*Universal Serial Bus */
		values.add("USENET"); /*User Network */
		values.add("USR"); /*US Robotics */
		values.add("UTP"); /*Unshielded Twisted Pair */
		values.add("UUCP"); /*UNIX-to-UNIX Copy */
		values.add("UUENCODE"); /*UNIX-to-UNIX Encode */
		values.add("VAR"); /*Value-Added Reseller */
		values.add("VAX"); /*Virtual Address Extension */
		values.add("VBE"); /*Vesa Bios Extension */
		values.add("VBNS"); /*Very High-speed Backbone Network Service */
		values.add("VCM SDRAM"); /*Virtual-Channel Memory SDRAM */
		values.add("VCPI"); /*Virtual Control Program Interface */
		values.add("VCSEL"); /*Vertical-Cavity Surface-Emitting Laser */
		values.add("VDSL"); /*Very High Bit Rate DSL */
		values.add("VDT"); /*Video Display Terminal */
		values.add("VERONICA"); /*Very Easy Rodent-Oriented Networkwide Index To Computerized Archives */
		values.add("VESA"); /*Video Electronics Standards Association */
		values.add("VFAT"); /*Virtual File Allocation Table */
		values.add("VFW"); /*Video For Windows */
		values.add("VGA"); /*Video Graphics Array (Adapter) */
		values.add("VLAN"); /*Virtual LAN */
		values.add("VLB"); /*VESA Local Bus (or VL Bus) */
		values.add("VME BUS"); /*VersaModule Eurocard Bus */
		values.add("VMS"); /*Virtual Memory System */
		values.add("VOB"); /*Voice Over Broadband */
		values.add("VODSL"); /*Voice Over Digital Subscriber Line */
		values.add("VOIP"); /*Voice Over Internet Protocol */
		values.add("VPN"); /*Virtual Private Network */
		values.add("VR"); /*Virtual Reality */
		values.add("VRAM"); /*Video Random Access Memory */
		values.add("VRM"); /*Voltage Regulator Module */
		values.add("VRML"); /*Virtual Reality Modeling Language */
		values.add("VRU"); /*Virus Research Unit */
		values.add("VSA"); /*Virtual Storage Architecture */
		values.add("VTAM"); /*Virtual Telecommunications Access Method */
		values.add("VTC"); /*Virus Test Center */
		values.add("W3C"); /*World Wide Web Consortium */
		values.add("WAFS"); /*Wide-area File Sharing */
		values.add("WAI"); /*Web Accessibility Initiative */
		values.add("WAIS"); /*Wide-Area Information Server */
		values.add("WAN"); /*Wide Area Network */
		values.add("WAP"); /*Wireless Application Protocol */
		values.add("WAP"); /*Wireless Access Protocol */
		values.add("WATS"); /*Web-based Assessment And Testing Systems */
		values.add("WATS"); /*Wide Area Telecommunications System */
		values.add("WCS"); /*World Coordinate System */
		values.add("WDM"); /*Wavelength Division Multiplexing */
		values.add("WEBCGM"); /*Web Computer Graphics Metafile */
		values.add("WEBINAR"); /*Web-based Seminar */
		values.add("WEP"); /*Wireless Equivalent Privacy */
		values.add("WFW"); /*Windows For Workgroups */
		values.add("WIAD"); /*Wireless Integrated Access Device */
		values.add("WIMP"); /*Windows, Icons, Menus And Pointing Device */
		values.add("WINCE"); /*Windows CE */
		values.add("WINS"); /*Windows Internet Name Service */
		values.add("WINSOCK"); /*Windows Socket */
		values.add("WINTEL"); /*Windows/Intel */
		values.add("WIT"); /*Wireless Information Terminal */
		values.add("WLAN"); /*Wireless Local Area Network */
		values.add("WLANA"); /*Wireless LAN Association */
		values.add("WMF"); /*Windows Meta File */
		values.add("WML"); /*Wireless Markup Language */
		values.add("WNIU"); /*Wireless Network Interface Unit */
		values.add("WORM"); /*Write Once Read Many */
		values.add("WOSA"); /*Windows Open Services Architecture */
		values.add("WP"); /*WordPerfect */
		values.add("WPA"); /*Wi-Fi Protected Access (WPA And WPA2) */
		values.add("WRAM"); /*Windows Random Access Memory */
		values.add("WSDL"); /*Web Services Description Language */
		values.add("WWW"); /*World-Wide Web */
		values.add("WYSIWYG"); /*What You See Is What You Get */
		values.add("WYSIWYP"); /*What You See Is What You Print */
		values.add("XDSL"); /*X Digital Subscriber Line */
		values.add("XGA"); /*Extended Graphics Array (Adapter) */
		values.add("XMI"); /*XML Metadata Interchange */
		values.add("XML"); /*Extensible Markup Language */
		values.add("XMLC"); /*Extensible Markup Language Compiler */
		values.add("XMS"); /*Extended Memory Specification */
		values.add("XPS"); /*XML Paper Specification */
		values.add("XSL"); /*Extensible Style Language */
		values.add("XT"); /*Extended */
		values.add("XTA"); /*XT (Extended) Attachment */
		values.add("XUL"); /*XML-based User Interface Language */
		values.add("Y2K"); /*Year 2000 */
		values.add("YAHOO"); /*Yet Another Hierarchical Officious Oracle */
		values.add("YTD"); /*Year To Date */
		values.add("ZAW"); /*Zero Administration For Windows */
		values.add("ZCAV"); /*Zoned Constant Angular Velocity */
		values.add("ZDI"); /*Zero Day Initiative */
		values.add("ZIF"); /*Zero Insertion Force */
		values.add("ZV"); /*Zoomed Video Port */
		values.add("ACL"); /*Access Control List */
		values.add("ADC"); /*Analog-to-Digital Converter */
		values.add("ADF"); /*Automatic Document Feeder */
		values.add("ADSL"); /*Asymmetric Digital Subscriber Line */
		values.add("AGP"); /*Accelerated Graphics Port */
		values.add("AIFF"); /*Audio Interchange File Format */
		values.add("AIX"); /*Advanced Interactive Executive */
		values.add("ANSI"); /*American National Standards Institute */
		values.add("API"); /*Application Program Interface */
		values.add("ARP"); /*Address Resolution Protocol */
		values.add("ASCII"); /*American Standard Code For Information Interchange */
		values.add("ASP"); /*Active Server Page */
		values.add("ASP"); /*Application Service Provider */
		values.add("ATA"); /*Advanced Technology Attachment */
		values.add("ATM"); /*Asynchronous Transfer Mode */
		values.add("BASIC"); /*Beginner's All-purpose Symbolic Instruction Code */
		values.add("BCC"); /*Blind Carbon Copy */
		values.add("BIOS"); /*Basic Input/Output System */
		values.add("BLOB"); /*Binary Large Object */
		values.add("BMP"); /*Bitmap */
		values.add("CAD"); /*Computer-Aided Design */
		values.add("CC"); /*Carbon Copy */
		values.add("CCD"); /*Charged Coupled Device */
		values.add("CD"); /*Compact Disc */
		values.add("CD-R"); /*Compact Disc Recordable */
		values.add("CD-ROM"); /*Compact Disc Read-Only Memory */
		values.add("CD-RW"); /*Compact Disc Re-Writable */
		values.add("CDMA"); /*Code Division Multiple Access */
		values.add("CGI"); /*Common Gateway Interface */
		values.add("CISC"); /*Complex Instruction Set Computing */
		values.add("CMOS"); /*Complementary Metal Oxide Semiconductor */
		values.add("CMYK"); /*Cyan Magenta Yellow Black */
		values.add("CPA"); /*Cost Per Action */
		values.add("CPC"); /*Cost Per Click */
		values.add("CPL"); /*Cost Per Lead */
		values.add("CPM"); /*Cost Per 1,000 Impressions */
		values.add("CPS"); /*Classroom Performance System */
		values.add("CPU"); /*Central Processing Unit */
		values.add("CRM"); /*Customer Relationship Management */
		values.add("CRT"); /*Cathode Ray Tube */
		values.add("CSS"); /*Cascading Style Sheet */
		values.add("CTP"); /*Composite Theoretical Performance */
		values.add("CTR"); /*Click-Through Rate */
		values.add("DAC"); /*Digital-to-Analog Converter */
		values.add("DBMS"); /*Database Management System */
		values.add("DDR"); /*Double Data Rate */
		values.add("DDR2"); /*Double Data Rate 2 */
		values.add("DHCP"); /*Dynamic Host Configuration Protocol */
		values.add("DIMM"); /*Dual In-Line Memory Module */
		values.add("DLL"); /*Dynamic Link Library */
		values.add("DMA"); /*Direct Memory Access */
		values.add("DNS"); /*Domain Name System */
		values.add("DOS"); /*Disk Operating System */
		values.add("DPI"); /*Dots Per Inch */
		values.add("DRAM"); /*Dynamic Random Access Memory */
		values.add("DRM"); /*Digital Rights Management */
		values.add("DSL"); /*Digital Subscriber Line */
		values.add("DSLAM"); /*Digital Subscriber Line Access Multiplexer */
		values.add("DTD"); /*Document Type Definition */
		values.add("DV"); /*Digital Video */
		values.add("DVD"); /*Digital Versatile Disc */
		values.add("DVD+R"); /*Digital Versatile Disc Recordable */
		values.add("DVD+RW"); /*Digital Versatile Disk Rewritable */
		values.add("DVD-R"); /*Digital Versatile Disc Recordable */
		values.add("DVD-RAM"); /*Digital Versatile Disc Random Access Memory */
		values.add("DVD-RW"); /*Digital Versatile Disk Rewritable */
		values.add("DVI"); /*Digital Video Interface */
		values.add("DVR"); /*Digital Video Recorder */
		values.add("ECC"); /*Error Correction Code */
		values.add("EDI"); /*Electronic Data Interchange */
		values.add("EPS"); /*Encapsulated PostScript */
		values.add("EUP"); /*Enterprise Unified Process */
		values.add("FAQ"); /*Frequently Asked Questions */
		values.add("FIFO"); /*First In, First Out */
		values.add("FIOS"); /*Fiber Optic Service */
		values.add("FLOPS"); /*Floating Point Operations Per Second */
		values.add("FPU"); /*Floating Point Unit */
		values.add("FSB"); /*Frontside Bus */
		values.add("FTP"); /*File Transfer Protocol */
		values.add("GIF"); /*Graphics Interchange Format; Applied To Image Format As .gif */
		values.add("GIGO"); /*Garbage In, Garbage Out */
		values.add("GIS"); /*Geographic Information Systems */
		values.add("GPS"); /*Global Positioning System */
		values.add("GPU"); /*Graphics Processing Unit */
		values.add("GUI"); /*Graphical User Interface */
		values.add("GUID"); /*Globally Unique Identifier */
		values.add("HDMI"); /*High-Definition Multimedia Interface */
		values.add("HDTV"); /*High Definition Televsion */
		values.add("HDV"); /*High-Definition Video */
		values.add("HFS"); /*Hierarchical File System */
		values.add("HSF"); /*Heat Sink And Fan */
		values.add("HTML"); /*Hyper-Text Markup Language */
		values.add("HTTP"); /*HyperText Transfer Protocol */
		values.add("HTTPS"); /*HyperText Transport Protocol Secure */
		values.add("I/O"); /*Input/Output */
		values.add("ICANN"); /*Internet Corporation For Assigned Names And Numbers */
		values.add("ICF"); /*Internet Connection Firewall */
		values.add("ICMP"); /*Internet Control Message Protocol */
		values.add("ICS"); /*Internet Connection Sharing */
		values.add("IDE"); /*Integrated Device Electronics */
		values.add("IDE"); /*Integrated Development Environment */
		values.add("IEEE"); /*Institute Of Electrical And Electronics Engineers */
		values.add("IGP"); /*Integrated Graphics Processor */
		values.add("IM"); /*Instant Message */
		values.add("IMAP"); /*Internet Message Access Protocol */
		values.add("INTERNIC"); /*Internet Network Information Center */
		values.add("IP"); /*Internet Protocol */
		values.add("IPX"); /*Internetwork Packet Exchange */
		values.add("IRC"); /*Internet Relay Chat */
		values.add("IRQ"); /*Interrupt Request */
		values.add("ISA"); /*Industry Standard Architecture */
		values.add("ISCSI"); /*Internet Small Computer Systems Interface */
		values.add("ISDN"); /*Integrated Services Digital Network */
		values.add("ISO"); /*International Organization For Standardization */
		values.add("ISP"); /*Internet Service Provider */
		values.add("IT"); /*Information Technology */
		values.add("IVR"); /*Interactive Voice Response */
		values.add("JPEG"); /*Joint Photographic Experts Group; Applied To Image Format As .jpeg */
		values.add("JRE"); /*Java Runtime Environment */
		values.add("JSP"); /*Java Server Page */
		values.add("KBPS"); /*Kilobits Per Second */
		values.add("KDE"); /*K Desktop Environment */
		values.add("KVM"); /*Switch Keyboard, Video, And Mouse Switch */
		values.add("LAN"); /*Local Area Network */
		values.add("LCD"); /*Liquid Crystal Display */
		values.add("LDAP"); /*Lightweight Directory Access Protocol */
		values.add("LIFO"); /*Last In, First Out */
		values.add("LPI"); /*Lines Per Inch */
		values.add("LUN"); /*Logical Unit Number */
		values.add("MAC (ADDRESS)"); /*Media Access Control Address */
		values.add("MANET"); /*Mobile Ad Hoc Network */
		values.add("MBPS"); /*Megabits Per Second */
		values.add("MCA"); /*Micro Channel Architecture */
		values.add("MIDI"); /*Musical Instrument Digital Interface */
		values.add("MIPS"); /*Million Instructions Per Second */
		values.add("MP3"); /*MPEG-1 Audio Layer-3 */
		values.add("MPEG"); /*Moving Picture Experts Group; Video Format */
		values.add("MTU"); /*Maximum Transmission Unit */
		values.add("NAT"); /*Network Address Translation */
		values.add("NETBIOS"); /*Network Basic Input/Output System */
		values.add("NIC"); /*Network Interface Card */
		values.add("NNTP"); /*Network News Transfer Protocol */
		values.add("NOC"); /*Network Operations Center */
		values.add("NTFS"); /*New Technology File System */
		values.add("OASIS"); /*Organization For The Advancement Of Structured Information Standards */
		values.add("OCR"); /*Optical Character Recognition */
		values.add("ODBC"); /*Open Database Connectivity */
		values.add("OEM"); /*Original Equipment Manufacturer */
		values.add("OLAP"); /*Online Analytical Processing */
		values.add("OLE"); /*Object Linking And Embedding */
		values.add("OOP"); /*Object-Oriented Programming */
		values.add("P2P"); /*Peer To Peer */
		values.add("PC"); /*Personal Computer */
		values.add("PCB"); /*Printed Circuit Board */
		values.add("PCI"); /*Peripheral Component Interconnect */
		values.add("PCI-X"); /*Peripheral Component Interconnect Extended */
		values.add("PCMCIA"); /*Personal Computer Memory Card International Association */
		values.add("PDA"); /*Personal Digital Assistant */
		values.add("PDF"); /*Portable Document Format */
		values.add("PHP"); /*Hypertext Preprocessor */
		values.add("PIM"); /*Personal Information Manager */
		values.add("PMU"); /*Power Management Unit */
		values.add("PNG"); /*Portable Network Graphic; Image Format */
		values.add("POP3"); /*Post Office Protocol */
		values.add("PPC"); /*Pay Per Click */
		values.add("PPGA"); /*Plastic Pin Grid Array */
		values.add("PPI"); /*Pixels Per Inch */
		values.add("PPL"); /*Pay Per Lead */
		values.add("PPM"); /*Pages Per Minute */
		values.add("PPP"); /*Point To Point Protocol */
		values.add("PPTP"); /*Point-to-Point Tunneling Protocol */
		values.add("PRAM"); /*Parameter Random Access Memory */
		values.add("PS/2"); /*Personal System/2 */
		values.add("QBE"); /*Query By Example */
		values.add("RAID"); /*Redundant Array Of Independent Disks */
		values.add("RAM"); /*Random Access Memory */
		values.add("RDF"); /*Resource Description Framework */
		values.add("RDRAM"); /*Rambus Dynamic Random Access Memory */
		values.add("RGB"); /*Red Green Blue */
		values.add("RISC"); /*Reduced Instruction Set Computing */
		values.add("ROM"); /*Read-Only Memory */
		values.add("RPC"); /*Remote Procedure Call */
		values.add("RPM"); /*Revenue Per 1,000 Impressions. (M Is The Roman Numeral For 1,000) */
		values.add("RSS"); /*RDF Site Summary */
		values.add("RTE"); /*Runtime Environment */
		values.add("RTF"); /*Rich Text Fomat */
		values.add("RUP"); /*Rational Unified Process */
		values.add("SAN"); /*Storage Area Network */
		values.add("SATA"); /*Serial Advanced Technology Attachment */
		values.add("SCSI"); /*Small Computer System Interface */
		values.add("SD"); /*Secure Digital */
		values.add("SDRAM"); /*Synchronous Dynamic Random Access Memory */
		values.add("SDSL"); /*Symmetric Digital Subscriber Line */
		values.add("SEM"); /*Search Engine Marketing */
		values.add("SEO"); /*Search Engine Optimization */
		values.add("SIMM"); /*Single In-Line Memory Module */
		values.add("SKU"); /*Stock Keeping Unit */
		values.add("SLI"); /*Scalable Link Interface */
		values.add("SMART"); /*Self-Monitoring Analysis And Reporting Technology */
		values.add("SMB"); /*Server Message Block */
		values.add("SMS"); /*Short Message Service */
		values.add("SMTP"); /*Simple Mail Transfer Protocol */
		values.add("SNMP"); /*Simple Network Management Protocol */
		values.add("SO-DIMM"); /*Small Outline Dual In-Line Memory Module */
		values.add("SOA"); /*Service Oriented Architecture */
		values.add("SOAP"); /*Simple Object Access Protocol */
		values.add("SQL"); /*Structured Query Language */
		values.add("SRAM"); /*Static Random Access Memory */
		values.add("SRGB"); /*Standard Red Green Blue */
		values.add("SSH"); /*Secure Shell */
		values.add("SSID"); /*Service Set Identifier */
		values.add("SSL"); /*Secure Sockets Layer */
		values.add("TCP/IP"); /*Transmission Control Protocol/Internet Protocol */
		values.add("TFT"); /*Thin-Film Transistor */
		values.add("TIFF"); /*Tagged Image File Format; Image Format */
		values.add("TTL"); /*Time To Live (DNS) */
		values.add("TWAIN"); /*Toolkit Without An Informative Name */
		values.add("UDDI"); /*Universal Description Discovery And Integration */
		values.add("UDP"); /*User Datagram Protocol */
		values.add("UML"); /*Unified Modeling Language */
		values.add("UNC"); /*Universal Naming Convention */
		values.add("UPNP"); /*Universal Plug And Play */
		values.add("UPS"); /*Uninterruptible Power Supply */
		values.add("URI"); /*Uniform Resource Identifier */
		values.add("URL"); /*Uniform Resource Locator */
		values.add("USB"); /*Universal Serial Bus */
		values.add("VCI"); /*Virtual Channel Identifier */
		values.add("VESA"); /*Video Electronics Standards Association */
		values.add("VFAT"); /*Virtual File Allocation Table */
		values.add("VGA"); /*Video Graphics Array */
		values.add("VLB"); /*VESA Local Bus */
		values.add("VLE"); /*Virtual Learning Environment */
		values.add("VOIP"); /*Voice Over Internet Protocol */
		values.add("VPI"); /*Virtual Path Identifier */
		values.add("VPN"); /*Virtual Private Network */
		values.add("VPS"); /*Virtual Private Server */
		values.add("VRAM"); /*Video Random Access Memory */
		values.add("VRML"); /*Virtual Reality Modeling Language */
		values.add("WAIS"); /*Wide Area Information Server */
		values.add("WAN"); /*Wide Area Network */
		values.add("WEP"); /*Wired Equivalent Privacy */
		values.add("WI-FI"); /*Wireless Fidelity */
		values.add("WPA"); /*Wi-Fi Protected Access */
		values.add("WWW"); /*World Wide Web */
		values.add("XHTML"); /*Extensible Hypertext Markup Language */
		values.add("XML"); /*Extensible Markup Language */
		values.add("XSLT"); /*Extensible Style Sheet Language Transformation */
		values.add("Y2K"); /*Year 2000 */
		values.add("ZIF"); /*Zero Insertion Force */
		values.add("UK"); /*United Kingdom */
		values.add("US"); /*United States */
		values.add("USA"); /*United States of America */
		values.add("AL"); /*Alabama */
		values.add("AK"); /*Alaska */
		values.add("AZ"); /*Arizona */
		values.add("AR"); /*Arkansas */
		values.add("CA"); /*California */
		values.add("CO"); /*Colorado */
		values.add("CT"); /*Connecticut */
		values.add("DE"); /*Delaware */
		values.add("DC"); /*District of Columbia */
		values.add("FL"); /*Florida */
		values.add("GA"); /*Georgia */
		values.add("HI"); /*Hawaii */
		values.add("ID"); /*Idaho */
		values.add("IL"); /*Illinois */
		values.add("IN"); /*Indiana */
		values.add("IA"); /*Iowa */
		values.add("KS"); /*Kansas */
		values.add("KY"); /*Kentucky */
		values.add("LA"); /*Louisiana */
		values.add("ME"); /*Maine */
		values.add("MD"); /*Maryland */
		values.add("MA"); /*Massachusetts */
		values.add("MI"); /*Michigan */
		values.add("MN"); /*Minnesota */
		values.add("MS"); /*Mississippi */
		values.add("MO"); /*Missouri */
		values.add("MT"); /*Montana */
		values.add("NE"); /*Nebraska */
		values.add("NV"); /*Nevada */
		values.add("NH"); /*New Hampshire */
		values.add("NJ"); /*New Jersey */
		values.add("NM"); /*New Mexico */
		values.add("NY"); /*New York */
		values.add("NC"); /*North Carolina */
		values.add("ND"); /*North Dakota */
		values.add("OH"); /*Ohio */
		values.add("OK"); /*Oklahoma */
		values.add("OR"); /*Oregon */
		values.add("PA"); /*Pennsylvania */
		values.add("RI"); /*Rhode Island */
		values.add("SC"); /*South Carolina */
		values.add("SD"); /*South Dakota */
		values.add("TN"); /*Tennessee */
		values.add("TX"); /*Texas */
		values.add("UT"); /*Utah */
		values.add("VT"); /*Vermont */
		values.add("VA"); /*Virginia */
		values.add("WA"); /*Washington */
		values.add("WV"); /*West Virginia */
		values.add("WI"); /*Wisconsin */
		values.add("WY"); /*Wyoming */
		values.add("AF"); /*AFGHANISTAN */
		values.add("AL"); /*ALBANIA */
		values.add("DZ"); /*ALGERIA */
		values.add("AS"); /*AMERICAN SAMOA */
		values.add("AD"); /*ANDORRA */
		values.add("AO"); /*ANGOLA */
		values.add("AI"); /*ANGUILLA */
		values.add("AQ"); /*ANTARCTICA */
		values.add("AG"); /*ANTIGUA AND BARBUDA */
		values.add("AR"); /*ARGENTINA */
		values.add("AM"); /*ARMENIA */
		values.add("AW"); /*ARUBA */
		values.add("AU"); /*AUSTRALIA */
		values.add("AT"); /*AUSTRIA */
		values.add("AZ"); /*AZERBAIJAN */
		values.add("BS"); /*BAHAMAS */
		values.add("BH"); /*BAHRAIN */
		values.add("BD"); /*BANGLADESH */
		values.add("BB"); /*BARBADOS */
		values.add("BY"); /*BELARUS */
		values.add("BE"); /*BELGIUM */
		values.add("BZ"); /*BELIZE */
		values.add("BJ"); /*BENIN */
		values.add("BM"); /*BERMUDA */
		values.add("BT"); /*BHUTAN */
		values.add("BO"); /*BOLIVIA */
		values.add("BA"); /*BOSNIA AND HERZEGOWINA */
		values.add("BW"); /*BOTSWANA */
		values.add("BV"); /*BOUVET ISLAND */
		values.add("BR"); /*BRAZIL */
		values.add("IO"); /*BRITISH INDIAN OCEAN TERRITORY */
		values.add("BN"); /*BRUNEI DARUSSALAM */
		values.add("BG"); /*BULGARIA */
		values.add("BF"); /*BURKINA FASO */
		values.add("BI"); /*BURUNDI */
		values.add("KH"); /*CAMBODIA */
		values.add("CM"); /*CAMEROON */
		values.add("CA"); /*CANADA */
		values.add("CV"); /*CAPE VERDE */
		values.add("KY"); /*CAYMAN ISLANDS */
		values.add("CF"); /*CENTRAL AFRICAN REPUBLIC */
		values.add("TD"); /*CHAD */
		values.add("CL"); /*CHILE */
		values.add("CN"); /*CHINA */
		values.add("CX"); /*CHRISTMAS ISLAND */
		values.add("CC"); /*COCOS (KEELING) ISLANDS */
		values.add("CO"); /*COLOMBIA */
		values.add("KM"); /*COMOROS */
		values.add("CG"); /*CONGO */
		values.add("CD"); /*CONGO, THE DRC */
		values.add("CK"); /*COOK ISLANDS */
		values.add("CR"); /*COSTA RICA */
		values.add("CI"); /*COTE D'IVOIRE */
		values.add("HR"); /*CROATIA (local name: Hrvatska) */
		values.add("CU"); /*CUBA */
		values.add("CY"); /*CYPRUS */
		values.add("CZ"); /*CZECH REPUBLIC */
		values.add("DK"); /*DENMARK */
		values.add("DJ"); /*DJIBOUTI */
		values.add("DM"); /*DOMINICA */
		values.add("DO"); /*DOMINICAN REPUBLIC */
		values.add("TP"); /*EAST TIMOR */
		values.add("EC"); /*ECUADOR */
		values.add("EG"); /*EGYPT */
		values.add("SV"); /*EL SALVADOR */
		values.add("GQ"); /*EQUATORIAL GUINEA */
		values.add("ER"); /*ERITREA */
		values.add("EE"); /*ESTONIA */
		values.add("ET"); /*ETHIOPIA */
		values.add("FK"); /*FALKLAND ISLANDS (MALVINAS) */
		values.add("FO"); /*FAROE ISLANDS */
		values.add("FJ"); /*FIJI */
		values.add("FI"); /*FINLAND */
		values.add("FR"); /*FRANCE */
		values.add("FX"); /*FRANCE, METROPOLITAN */
		values.add("GF"); /*FRENCH GUIANA */
		values.add("PF"); /*FRENCH POLYNESIA */
		values.add("TF"); /*FRENCH SOUTHERN TERRITORIES */
		values.add("GA"); /*GABON */
		values.add("GB"); /*GREAT BRITAIN */
		values.add("GM"); /*GAMBIA */
		values.add("GE"); /*GEORGIA */
		values.add("DE"); /*GERMANY */
		values.add("GH"); /*GHANA */
		values.add("GI"); /*GIBRALTAR */
		values.add("GR"); /*GREECE */
		values.add("GL"); /*GREENLAND */
		values.add("GD"); /*GRENADA */
		values.add("GP"); /*GUADELOUPE */
		values.add("GU"); /*GUAM */
		values.add("GT"); /*GUATEMALA */
		values.add("GN"); /*GUINEA */
		values.add("GW"); /*GUINEA-BISSAU */
		values.add("GY"); /*GUYANA */
		values.add("HT"); /*HAITI */
		values.add("HM"); /*HEARD AND MC DONALD ISLANDS */
		values.add("VA"); /*HOLY SEE (VATICAN CITY STATE) */
		values.add("HN"); /*HONDURAS */
		values.add("HK"); /*HONG KONG */
		values.add("HU"); /*HUNGARY */
		values.add("IS"); /*ICELAND */
		values.add("IN"); /*INDIA */
		values.add("ID"); /*INDONESIA */
		values.add("IR"); /*IRAN (ISLAMIC REPUBLIC OF) */
		values.add("IQ"); /*IRAQ */
		values.add("IE"); /*IRELAND */
		values.add("IL"); /*ISRAEL */
		values.add("IT"); /*ITALY */
		values.add("JM"); /*JAMAICA */
		values.add("JP"); /*JAPAN */
		values.add("JO"); /*JORDAN */
		values.add("KZ"); /*KAZAKHSTAN */
		values.add("KE"); /*KENYA */
		values.add("KI"); /*KIRIBATI */
		values.add("KP"); /*KOREA, D.P.R.O. */
		values.add("KR"); /*KOREA, REPUBLIC OF */
		values.add("KW"); /*KUWAIT */
		values.add("KG"); /*KYRGYZSTAN */
		values.add("LA"); /*LAOSÊ */
		values.add("LV"); /*LATVIA */
		values.add("LB"); /*LEBANON */
		values.add("LS"); /*LESOTHO */
		values.add("LR"); /*LIBERIA */
		values.add("LY"); /*LIBYAN ARAB JAMAHIRIYA */
		values.add("LI"); /*LIECHTENSTEIN */
		values.add("LT"); /*LITHUANIA */
		values.add("LU"); /*LUXEMBOURG */
		values.add("MO"); /*MACAU */
		values.add("MK"); /*MACEDONIA */
		values.add("MG"); /*MADAGASCAR */
		values.add("MW"); /*MALAWI */
		values.add("MY"); /*MALAYSIA */
		values.add("MV"); /*MALDIVES */
		values.add("ML"); /*MALI */
		values.add("MT"); /*MALTA */
		values.add("MH"); /*MARSHALL ISLANDS */
		values.add("MQ"); /*MARTINIQUE */
		values.add("MR"); /*MAURITANIA */
		values.add("MU"); /*MAURITIUS */
		values.add("YT"); /*MAYOTTE */
		values.add("MX"); /*MEXICO */
		values.add("FM"); /*MICRONESIA, FEDERATED STATES OF */
		values.add("MD"); /*MOLDOVA, REPUBLIC OF */
		values.add("MC"); /*MONACO */
		values.add("MN"); /*MONGOLIA */
		values.add("ME"); /*MONTENEGRO */
		values.add("MS"); /*MONTSERRAT */
		values.add("MA"); /*MOROCCO */
		values.add("MZ"); /*MOZAMBIQUE */
		values.add("MM"); /*MYANMAR (Burma)Ê */
		values.add("NA"); /*NAMIBIA */
		values.add("NR"); /*NAURU */
		values.add("NP"); /*NEPAL */
		values.add("NL"); /*NETHERLANDS */
		values.add("AN"); /*NETHERLANDS ANTILLES */
		values.add("NC"); /*NEW CALEDONIA */
		values.add("NZ"); /*NEW ZEALAND */
		values.add("NI"); /*NICARAGUA */
		values.add("NE"); /*NIGER */
		values.add("NG"); /*NIGERIA */
		values.add("NU"); /*NIUE */
		values.add("NF"); /*NORFOLK ISLAND */
		values.add("MP"); /*NORTHERN MARIANA ISLANDS */
		values.add("NO"); /*NORWAY */
		values.add("OM"); /*OMAN */
		values.add("PK"); /*PAKISTAN */
		values.add("PW"); /*PALAU */
		values.add("PA"); /*PANAMA */
		values.add("PG"); /*PAPUA NEW GUINEA */
		values.add("PY"); /*PARAGUAY */
		values.add("PE"); /*PERU */
		values.add("PH"); /*PHILIPPINES */
		values.add("PN"); /*PITCAIRN */
		values.add("PL"); /*POLAND */
		values.add("PT"); /*PORTUGAL */
		values.add("PR"); /*PUERTO RICO */
		values.add("QA"); /*QATAR */
		values.add("RE"); /*REUNION */
		values.add("RO"); /*ROMANIA */
		values.add("RU"); /*RUSSIAN FEDERATION */
		values.add("RW"); /*RWANDA */
		values.add("KN"); /*SAINT KITTS AND NEVIS */
		values.add("LC"); /*SAINT LUCIA */
		values.add("VC"); /*SAINT VINCENT AND THE GRENADINES */
		values.add("WS"); /*SAMOA */
		values.add("SM"); /*SAN MARINO */
		values.add("ST"); /*SAO TOME AND PRINCIPE */
		values.add("SA"); /*SAUDI ARABIA */
		values.add("SN"); /*SENEGAL */
		values.add("RS"); /*SERBIA */
		values.add("SC"); /*SEYCHELLES */
		values.add("SL"); /*SIERRA LEONE */
		values.add("SG"); /*SINGAPORE */
		values.add("SK"); /*SLOVAKIA (Slovak Republic) */
		values.add("SI"); /*SLOVENIA */
		values.add("SB"); /*SOLOMON ISLANDS */
		values.add("SO"); /*SOMALIA */
		values.add("ZA"); /*SOUTH AFRICA */
		values.add("SS"); /*SOUTH SUDAN */
		values.add("GS"); /*SOUTH GEORGIA AND SOUTH S.S. */
		values.add("ES"); /*SPAIN */
		values.add("LK"); /*SRI LANKA */
		values.add("SH"); /*ST. HELENA */
		values.add("PM"); /*ST. PIERRE AND MIQUELON */
		values.add("SD"); /*SUDAN */
		values.add("SR"); /*SURINAME */
		values.add("SJ"); /*SVALBARD AND JAN MAYEN ISLANDS */
		values.add("SZ"); /*SWAZILAND */
		values.add("SE"); /*SWEDEN */
		values.add("CH"); /*SWITZERLAND */
		values.add("SY"); /*SYRIAN ARAB REPUBLIC */
		values.add("TW"); /*TAIWAN, PROVINCE OF CHINA */
		values.add("TJ"); /*TAJIKISTAN */
		values.add("TZ"); /*TANZANIA, UNITED REPUBLIC OF */
		values.add("TH"); /*THAILAND */
		values.add("TG"); /*TOGO */
		values.add("TK"); /*TOKELAU */
		values.add("TO"); /*TONGA */
		values.add("TT"); /*TRINIDAD AND TOBAGO */
		values.add("TN"); /*TUNISIA */
		values.add("TR"); /*TURKEY */
		values.add("TM"); /*TURKMENISTAN */
		values.add("TC"); /*TURKS AND CAICOS ISLANDS */
		values.add("TV"); /*TUVALU */
		values.add("UG"); /*UGANDA */
		values.add("UA"); /*UKRAINE */
		values.add("AE"); /*UNITED ARAB EMIRATES */
		values.add("GB"); /*UNITED KINGDOM */
		values.add("US"); /*UNITED STATES */
		values.add("UM"); /*U.S. MINOR ISLANDS */
		values.add("UY"); /*URUGUAY */
		values.add("UZ"); /*UZBEKISTAN */
		values.add("VU"); /*VANUATU */
		values.add("VE"); /*VENEZUELA */
		values.add("VN"); /*VIET NAM */
		values.add("VG"); /*VIRGIN ISLANDS (BRITISH) */
		values.add("VI"); /*VIRGIN ISLANDS (U.S.) */
		values.add("WF"); /*WALLIS AND FUTUNA ISLANDS */
		values.add("EH"); /*WESTERN SAHARA */
		values.add("YE"); /*YEMEN */
		values.add("ZM"); /*ZAMBIA */
		values.add("ZW"); /*ZIMBABWEÊ */
		values.add("AFG"); /*AFGHANISTAN */
		values.add("ALB"); /*ALBANIA */
		values.add("DZA"); /*ALGERIA */
		values.add("ASM"); /*AMERICAN SAMOA */
		values.add("AND"); /*ANDORRA */
		values.add("AGO"); /*ANGOLA */
		values.add("AIA"); /*ANGUILLA */
		values.add("ATA"); /*ANTARCTICA */
		values.add("ATG"); /*ANTIGUA AND BARBUDA */
		values.add("ARG"); /*ARGENTINA */
		values.add("ARM"); /*ARMENIA */
		values.add("ABW"); /*ARUBA */
		values.add("AUS"); /*AUSTRALIA */
		values.add("AUT"); /*AUSTRIA */
		values.add("AZE"); /*AZERBAIJAN */
		values.add("BHS"); /*BAHAMAS */
		values.add("BHR"); /*BAHRAIN */
		values.add("BGD"); /*BANGLADESH */
		values.add("BRB"); /*BARBADOS */
		values.add("BLR"); /*BELARUS */
		values.add("BEL"); /*BELGIUM */
		values.add("BLZ"); /*BELIZE */
		values.add("BEN"); /*BENIN */
		values.add("BMU"); /*BERMUDA */
		values.add("BTN"); /*BHUTAN */
		values.add("BOL"); /*BOLIVIA */
		values.add("BIH"); /*BOSNIA AND HERZEGOWINA */
		values.add("BWA"); /*BOTSWANA */
		values.add("BVT"); /*BOUVET ISLAND */
		values.add("BRA"); /*BRAZIL */
		values.add("IOT"); /*BRITISH INDIAN OCEAN TERRITORY */
		values.add("BRN"); /*BRUNEI DARUSSALAM */
		values.add("BGR"); /*BULGARIA */
		values.add("BFA"); /*BURKINA FASO */
		values.add("BDI"); /*BURUNDI */
		values.add("KHM"); /*CAMBODIA */
		values.add("CMR"); /*CAMEROON */
		values.add("CAN"); /*CANADA */
		values.add("CPV"); /*CAPE VERDE */
		values.add("CYM"); /*CAYMAN ISLANDS */
		values.add("CAF"); /*CENTRAL AFRICAN REPUBLIC */
		values.add("TCD"); /*CHAD */
		values.add("CHL"); /*CHILE */
		values.add("CHN"); /*CHINA */
		values.add("CXR"); /*CHRISTMAS ISLAND */
		values.add("CCK"); /*COCOS (KEELING) ISLANDS */
		values.add("COL"); /*COLOMBIA */
		values.add("COM"); /*COMOROS */
		values.add("COG"); /*CONGO */
		values.add("COD"); /*CONGO, THE DRC */
		values.add("COK"); /*COOK ISLANDS */
		values.add("CRI"); /*COSTA RICA */
		values.add("CIV"); /*COTE D'IVOIRE */
		values.add("HRV"); /*CROATIA (local name: Hrvatska) */
		values.add("CUB"); /*CUBA */
		values.add("CYP"); /*CYPRUS */
		values.add("CZE"); /*CZECH REPUBLIC */
		values.add("DNK"); /*DENMARK */
		values.add("DJI"); /*DJIBOUTI */
		values.add("DMA"); /*DOMINICA */
		values.add("DOM"); /*DOMINICAN REPUBLIC */
		values.add("TMP"); /*EAST TIMOR */
		values.add("ECU"); /*ECUADOR */
		values.add("EGY"); /*EGYPT */
		values.add("SLV"); /*EL SALVADOR */
		values.add("GNQ"); /*EQUATORIAL GUINEA */
		values.add("ERI"); /*ERITREA */
		values.add("EST"); /*ESTONIA */
		values.add("ETHÊ"); /*ETHIOPIA */
		values.add("FLK"); /*FALKLAND ISLANDS (MALVINAS) */
		values.add("FRO"); /*FAROE ISLANDS */
		values.add("FJI"); /*FIJI */
		values.add("FIN"); /*FINLAND */
		values.add("FRA"); /*FRANCE */
		values.add("FXX"); /*FRANCE, METROPOLITAN */
		values.add("GUF"); /*FRENCH GUIANA */
		values.add("PYF"); /*FRENCH POLYNESIA */
		values.add("ATF"); /*FRENCH SOUTHERN TERRITORIES */
		values.add("GAB"); /*GABON */
		values.add("GMB"); /*GAMBIA */
		values.add("GEO"); /*GEORGIA */
		values.add("DEU"); /*GERMANY */
		values.add("GHA"); /*GHANA */
		values.add("GIB"); /*GIBRALTAR */
		values.add("GRC"); /*GREECE */
		values.add("GRL"); /*GREENLAND */
		values.add("GRD"); /*GRENADA */
		values.add("GLP"); /*GUADELOUPE */
		values.add("GUM"); /*GUAM */
		values.add("GTM"); /*GUATEMALA */
		values.add("GIN"); /*GUINEA */
		values.add("GNB"); /*GUINEA-BISSAU */
		values.add("GUY"); /*GUYANA */
		values.add("HTI"); /*HAITI */
		values.add("HMD"); /*HEARD AND MC DONALD ISLANDS */
		values.add("VAT"); /*HOLY SEE (VATICAN CITY STATE) */
		values.add("HND"); /*HONDURAS */
		values.add("HKG"); /*HONG KONG */
		values.add("HUN"); /*HUNGARY */
		values.add("ISL"); /*ICELAND */
		values.add("IND"); /*INDIA */
		values.add("IDN"); /*INDONESIA */
		values.add("IRN"); /*IRAN (ISLAMIC REPUBLIC OF) */
		values.add("IRQ"); /*IRAQ */
		values.add("IRL"); /*IRELAND */
		values.add("ISR"); /*ISRAEL */
		values.add("ITA"); /*ITALY */
		values.add("JAM"); /*JAMAICA */
		values.add("JPN"); /*JAPAN */
		values.add("JOR"); /*JORDAN */
		values.add("KAZ"); /*KAZAKHSTAN */
		values.add("KEN"); /*KENYA */
		values.add("KIR"); /*KIRIBATI */
		values.add("PRK"); /*KOREA, D.P.R.O. */
		values.add("KOR"); /*KOREA, REPUBLIC OF */
		values.add("KWT"); /*KUWAIT */
		values.add("KGZ"); /*KYRGYZSTAN */
		values.add("LAO"); /*LAOSÊ */
		values.add("LVA"); /*LATVIA */
		values.add("LBN"); /*LEBANON */
		values.add("LSO"); /*LESOTHO */
		values.add("LBR"); /*LIBERIA */
		values.add("LBY"); /*LIBYAN ARAB JAMAHIRIYA */
		values.add("LIE"); /*LIECHTENSTEIN */
		values.add("LTU"); /*LITHUANIA */
		values.add("LUX"); /*LUXEMBOURG */
		values.add("MAC"); /*MACAU */
		values.add("MKD"); /*MACEDONIA */
		values.add("MDG"); /*MADAGASCAR */
		values.add("MWI"); /*MALAWI */
		values.add("MYS"); /*MALAYSIA */
		values.add("MDV"); /*MALDIVES */
		values.add("MLI"); /*MALI */
		values.add("MLT"); /*MALTA */
		values.add("MHL"); /*MARSHALL ISLANDS */
		values.add("MTQ"); /*MARTINIQUE */
		values.add("MRT"); /*MAURITANIA */
		values.add("MUS"); /*MAURITIUS */
		values.add("MYT"); /*MAYOTTE */
		values.add("MEX"); /*MEXICO */
		values.add("FSM"); /*MICRONESIA, FEDERATED STATES OF */
		values.add("MDA"); /*MOLDOVA, REPUBLIC OF */
		values.add("MCO"); /*MONACO */
		values.add("MNG"); /*MONGOLIA */
		values.add("MNE"); /*MONTENEGRO */
		values.add("MSR"); /*MONTSERRAT */
		values.add("MAR"); /*MOROCCO */
		values.add("MOZ"); /*MOZAMBIQUE */
		values.add("MMR"); /*MYANMAR (Burma)Ê */
		values.add("NAM"); /*NAMIBIA */
		values.add("NRU"); /*NAURU */
		values.add("NPL"); /*NEPAL */
		values.add("NLD"); /*NETHERLANDS */
		values.add("ANT"); /*NETHERLANDS ANTILLES */
		values.add("NCL"); /*NEW CALEDONIA */
		values.add("NZL"); /*NEW ZEALAND */
		values.add("NIC"); /*NICARAGUA */
		values.add("NER"); /*NIGER */
		values.add("NGA"); /*NIGERIA */
		values.add("NIU"); /*NIUE */
		values.add("NFK"); /*NORFOLK ISLAND */
		values.add("MNP"); /*NORTHERN MARIANA ISLANDS */
		values.add("NOR"); /*NORWAY */
		values.add("OMN"); /*OMAN */
		values.add("PAK"); /*PAKISTAN */
		values.add("PLW"); /*PALAU */
		values.add("PAN"); /*PANAMA */
		values.add("PNG"); /*PAPUA NEW GUINEA */
		values.add("PRY"); /*PARAGUAY */
		values.add("PER"); /*PERU */
		values.add("PHL"); /*PHILIPPINES */
		values.add("PCN"); /*PITCAIRN */
		values.add("POL"); /*POLAND */
		values.add("PRT"); /*PORTUGAL */
		values.add("PRI"); /*PUERTO RICO */
		values.add("QAT"); /*QATAR */
		values.add("REU"); /*REUNION */
		values.add("ROM"); /*ROMANIA */
		values.add("RUS"); /*RUSSIAN FEDERATION */
		values.add("RWA"); /*RWANDA */
		values.add("KNA"); /*SAINT KITTS AND NEVIS */
		values.add("LCA"); /*SAINT LUCIA */
		values.add("VCT"); /*SAINT VINCENT AND THE GRENADINES */
		values.add("WSM"); /*SAMOA */
		values.add("SMR"); /*SAN MARINO */
		values.add("STP"); /*SAO TOME AND PRINCIPE */
		values.add("SAU"); /*SAUDI ARABIA */
		values.add("SEN"); /*SENEGAL */
		values.add("SRB"); /*SERBIA */
		values.add("SYC"); /*SEYCHELLES */
		values.add("SLE"); /*SIERRA LEONE */
		values.add("SGP"); /*SINGAPORE */
		values.add("SVK"); /*SLOVAKIA (Slovak Republic) */
		values.add("SVN"); /*SLOVENIA */
		values.add("SLB"); /*SOLOMON ISLANDS */
		values.add("SOM"); /*SOMALIA */
		values.add("ZAF"); /*SOUTH AFRICA */
		values.add("SSD"); /*SOUTH SUDAN */
		values.add("SGS"); /*SOUTH GEORGIA AND SOUTH S.S. */
		values.add("ESP"); /*SPAIN */
		values.add("LKA"); /*SRI LANKA */
		values.add("SHN"); /*ST. HELENA */
		values.add("SPM"); /*ST. PIERRE AND MIQUELON */
		values.add("SDN"); /*SUDAN */
		values.add("SUR"); /*SURINAME */
		values.add("SJM"); /*SVALBARD AND JAN MAYEN ISLANDS */
		values.add("SWZ"); /*SWAZILAND */
		values.add("SWE"); /*SWEDEN */
		values.add("CHE"); /*SWITZERLAND */
		values.add("SYR"); /*SYRIAN ARAB REPUBLIC */
		values.add("TWN"); /*TAIWAN, PROVINCE OF CHINA */
		values.add("TJK"); /*TAJIKISTAN */
		values.add("TZA"); /*TANZANIA, UNITED REPUBLIC OF */
		values.add("THA"); /*THAILAND */
		values.add("TGO"); /*TOGO */
		values.add("TKL"); /*TOKELAU */
		values.add("TON"); /*TONGA */
		values.add("TTO"); /*TRINIDAD AND TOBAGO */
		values.add("TUN"); /*TUNISIA */
		values.add("TUR"); /*TURKEY */
		values.add("TKM"); /*TURKMENISTAN */
		values.add("TCA"); /*TURKS AND CAICOS ISLANDS */
		values.add("TUV"); /*TUVALU */
		values.add("UGA"); /*UGANDA */
		values.add("UKR"); /*UKRAINE */
		values.add("ARE"); /*UNITED ARAB EMIRATES */
		values.add("GBR"); /*UNITED KINGDOM */
		values.add("USA"); /*UNITED STATES */
		values.add("UMI"); /*U.S. MINOR ISLANDS */
		values.add("URY"); /*URUGUAY */
		values.add("UZB"); /*UZBEKISTAN */
		values.add("VUT"); /*VANUATU */
		values.add("VEN"); /*VENEZUELA */
		values.add("VNM"); /*VIET NAM */
		values.add("VGB"); /*VIRGIN ISLANDS (BRITISH) */
		values.add("VIR"); /*VIRGIN ISLANDS (U.S.) */
		values.add("WLF"); /*WALLIS AND FUTUNA ISLANDS */
		values.add("ESH"); /*WESTERN SAHARA */
		values.add("YEM"); /*YEMEN */
		values.add("ZMB"); /*ZAMBIA */
		values.add("ZWEÊ"); /*ZIMBABWEÊ */
		values.add("AED"); /*United Arab Emirates Dirham */
		values.add("AFN"); /*Afghanistan Afghani */
		values.add("ALL"); /*Albania Lek */
		values.add("AMD"); /*Armenia Dram */
		values.add("ANG"); /*Netherlands Antilles Guilder */
		values.add("AOA"); /*Angola Kwanza */
		values.add("ARS"); /*Argentina Peso */
		values.add("AUD"); /*Australia Dollar */
		values.add("AWG"); /*Aruba Guilder */
		values.add("AZN"); /*Azerbaijan New Manat */
		values.add("BAM"); /*Bosnia and Herzegovina Convertible Marka */
		values.add("BBD"); /*Barbados Dollar */
		values.add("BDT"); /*Bangladesh Taka */
		values.add("BGN"); /*Bulgaria Lev */
		values.add("BHD"); /*Bahrain Dinar */
		values.add("BIF"); /*Burundi Franc */
		values.add("BMD"); /*Bermuda Dollar */
		values.add("BND"); /*Brunei Darussalam Dollar */
		values.add("BOB"); /*Bolivia Boliviano */
		values.add("BRL"); /*Brazil Real */
		values.add("BSD"); /*Bahamas Dollar */
		values.add("BTN"); /*Bhutan Ngultrum */
		values.add("BWP"); /*Botswana Pula */
		values.add("BYR"); /*Belarus Ruble */
		values.add("BZD"); /*Belize Dollar */
		values.add("CAD"); /*Canada Dollar */
		values.add("CDF"); /*Congo/Kinshasa Franc */
		values.add("CHF"); /*Switzerland Franc */
		values.add("CLP"); /*Chile Peso */
		values.add("CNY"); /*China Yuan Renminbi */
		values.add("COP"); /*Colombia Peso */
		values.add("CRC"); /*Costa Rica Colon */
		values.add("CUC"); /*Cuba Convertible Peso */
		values.add("CUP"); /*Cuba Peso */
		values.add("CVE"); /*Cape Verde Escudo */
		values.add("CZK"); /*Czech Republic Koruna */
		values.add("DJF"); /*Djibouti Franc */
		values.add("DKK"); /*Denmark Krone */
		values.add("DOP"); /*Dominican Republic Peso */
		values.add("DZD"); /*Algeria Dinar */
		values.add("EGP"); /*Egypt Pound */
		values.add("ERN"); /*Eritrea Nakfa */
		values.add("ETB"); /*Ethiopia Birr */
		values.add("EUR"); /*Euro Member Countries */
		values.add("FJD"); /*Fiji Dollar */
		values.add("FKP"); /*Falkland Islands (Malvinas) Pound */
		values.add("GBP"); /*United Kingdom Pound */
		values.add("GEL"); /*Georgia Lari */
		values.add("GGP"); /*Guernsey Pound */
		values.add("GHS"); /*Ghana Cedi */
		values.add("GIP"); /*Gibraltar Pound */
		values.add("GMD"); /*Gambia Dalasi */
		values.add("GNF"); /*Guinea Franc */
		values.add("GTQ"); /*Guatemala Quetzal */
		values.add("GYD"); /*Guyana Dollar */
		values.add("HKD"); /*Hong Kong Dollar */
		values.add("HNL"); /*Honduras Lempira */
		values.add("HRK"); /*Croatia Kuna */
		values.add("HTG"); /*Haiti Gourde */
		values.add("HUF"); /*Hungary Forint */
		values.add("IDR"); /*Indonesia Rupiah */
		values.add("ILS"); /*Israel Shekel */
		values.add("IMP"); /*Isle of Man Pound */
		values.add("INR"); /*India Rupee */
		values.add("IQD"); /*Iraq Dinar */
		values.add("IRR"); /*Iran Rial */
		values.add("ISK"); /*Iceland Krona */
		values.add("JEP"); /*Jersey Pound */
		values.add("JMD"); /*Jamaica Dollar */
		values.add("JOD"); /*Jordan Dinar */
		values.add("JPY"); /*Japan Yen */
		values.add("KES"); /*Kenya Shilling */
		values.add("KGS"); /*Kyrgyzstan Som */
		values.add("KHR"); /*Cambodia Riel */
		values.add("KMF"); /*Comoros Franc */
		values.add("KPW"); /*Korea (North) Won */
		values.add("KRW"); /*Korea (South) Won */
		values.add("KWD"); /*Kuwait Dinar */
		values.add("KYD"); /*Cayman Islands Dollar */
		values.add("KZT"); /*Kazakhstan Tenge */
		values.add("LAK"); /*Laos Kip */
		values.add("LBP"); /*Lebanon Pound */
		values.add("LKR"); /*Sri Lanka Rupee */
		values.add("LRD"); /*Liberia Dollar */
		values.add("LSL"); /*Lesotho Loti */
		values.add("LTL"); /*Lithuania Litas */
		values.add("LVL"); /*Latvia Lat */
		values.add("LYD"); /*Libya Dinar */
		values.add("MAD"); /*Morocco Dirham */
		values.add("MDL"); /*Moldova Leu */
		values.add("MGA"); /*Madagascar Ariary */
		values.add("MKD"); /*Macedonia Denar */
		values.add("MMK"); /*Myanmar (Burma) Kyat */
		values.add("MNT"); /*Mongolia Tughrik */
		values.add("MOP"); /*Macau Pataca */
		values.add("MRO"); /*Mauritania Ouguiya */
		values.add("MUR"); /*Mauritius Rupee */
		values.add("MVR"); /*Maldives (Maldive Islands) Rufiyaa */
		values.add("MWK"); /*Malawi Kwacha */
		values.add("MXN"); /*Mexico Peso */
		values.add("MYR"); /*Malaysia Ringgit */
		values.add("MZN"); /*Mozambique Metical */
		values.add("NAD"); /*Namibia Dollar */
		values.add("NGN"); /*Nigeria Naira */
		values.add("NIO"); /*Nicaragua Cordoba */
		values.add("NOK"); /*Norway Krone */
		values.add("NPR"); /*Nepal Rupee */
		values.add("NZD"); /*New Zealand Dollar */
		values.add("OMR"); /*Oman Rial */
		values.add("PAB"); /*Panama Balboa */
		values.add("PEN"); /*Peru Nuevo Sol */
		values.add("PGK"); /*Papua New Guinea Kina */
		values.add("PHP"); /*Philippines Peso */
		values.add("PKR"); /*Pakistan Rupee */
		values.add("PLN"); /*Poland Zloty */
		values.add("PYG"); /*Paraguay Guarani */
		values.add("QAR"); /*Qatar Riyal */
		values.add("RON"); /*Romania New Leu */
		values.add("RSD"); /*Serbia Dinar */
		values.add("RUB"); /*Russia Ruble */
		values.add("RWF"); /*Rwanda Franc */
		values.add("SAR"); /*Saudi Arabia Riyal */
		values.add("SBD"); /*Solomon Islands Dollar */
		values.add("SCR"); /*Seychelles Rupee */
		values.add("SDG"); /*Sudan Pound */
		values.add("SEK"); /*Sweden Krona */
		values.add("SGD"); /*Singapore Dollar */
		values.add("SHP"); /*Saint Helena Pound */
		values.add("SLL"); /*Sierra Leone Leone */
		values.add("SOS"); /*Somalia Shilling */
		values.add("SPL*"); /*Seborga Luigino */
		values.add("SRD"); /*Suriname Dollar */
		values.add("STD"); /*S‹o TomŽ and Pr’ncipe Dobra */
		values.add("SVC"); /*El Salvador Colon */
		values.add("SYP"); /*Syria Pound */
		values.add("SZL"); /*Swaziland Lilangeni */
		values.add("THB"); /*Thailand Baht */
		values.add("TJS"); /*Tajikistan Somoni */
		values.add("TMT"); /*Turkmenistan Manat */
		values.add("TND"); /*Tunisia Dinar */
		values.add("TOP"); /*Tonga Pa'anga */
		values.add("TRY"); /*Turkey Lira */
		values.add("TTD"); /*Trinidad and Tobago Dollar */
		values.add("TVD"); /*Tuvalu Dollar */
		values.add("TWD"); /*Taiwan New Dollar */
		values.add("TZS"); /*Tanzania Shilling */
		values.add("UAH"); /*Ukraine Hryvna */
		values.add("UGX"); /*Uganda Shilling */
		values.add("USD"); /*United States Dollar */
		values.add("UYU"); /*Uruguay Peso */
		values.add("UZS"); /*Uzbekistan Som */
		values.add("VEF"); /*Venezuela Bolivar */
		values.add("VND"); /*Viet Nam Dong */
		values.add("VUV"); /*Vanuatu Vatu */
		values.add("WST"); /*Samoa Tala */
		values.add("XAF"); /*CommunautŽ Financire Africaine (BEAC) CFA FrancÊBEAC */
		values.add("XCD"); /*East Caribbean Dollar */
		values.add("XDR"); /*International Monetary Fund (IMF) Special Drawing Rights */
		values.add("XOF"); /*CommunautŽ Financire Africaine (BCEAO) Franc */
		values.add("XPF"); /*Comptoirs Franais du Pacifique (CFP) Franc */
		values.add("YER"); /*Yemen Rial */
		values.add("ZAR"); /*South Africa Rand */
		values.add("ZMW"); /*Zambia Kwacha */
		values.add("ZWD"); /*Zimbabwe Dollar */
		
		values.add("PS");  /*Postscript */
		values.add("VS");  /*versus     */
		values.add("QS");  /*questions  */
		
		DEFAULT_ACRONYMS = values;
	};
	
	public final static AcronymSet INSTANCE = new AcronymSet();
	
	private final Set<String> acronyms;
	
	public AcronymSet()
	{	this.acronyms = DEFAULT_ACRONYMS;
	}
	
	public AcronymSet(Set<String> acronyms)
	{	this.acronyms = new HashSet<>(acronyms.size());
		for (String acronym : acronyms)
			this.acronyms.add (acronym.toUpperCase());
	}
	
	public boolean contains (String acronym)
	{	return acronym != null && acronyms.contains (acronym.toUpperCase());
	}
}
