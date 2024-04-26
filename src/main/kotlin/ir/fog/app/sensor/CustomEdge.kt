import java.io.Serializable

class CustomAppEdge: Serializable {
    /**
     * Name of source application module
     */
    var source: String? = null

    /**
     * Name of destination application module
     */
    var destination: String? = null

    /**
     * CPU length (in MIPS) of tuples carried by the application edge
     */
    var tupleCpuLength = 0.0

    /**
     * Network length (in bytes) of tuples carried by the application edge
     */
    var tupleNwLength = 0.0

    /**
     * Type of tuples carried by the application edge
     */
    var tupleType: String? = null

    /**
     * Direction of tuples carried by the application edge.
     */
    var direction = 0
    var edgeType = 0

    /**
     * Periodicity of application edge (in case it is periodic).
     */
    var periodicity = 0.0

    /**
     * Denotes if the application edge is a periodic edge.
     */
    var isPeriodic = false

    constructor() {}
    constructor(
        source: String?, destination: String?, tupleCpuLength: Double,
        tupleNwLength: Double, tupleType: String?, direction: Int, edgeType: Int
    ) {
        this.source = source
        this.destination = destination
        this.tupleCpuLength = tupleCpuLength
        this.tupleNwLength = tupleNwLength
        this.tupleType = tupleType
        this.direction = direction
        this.edgeType = edgeType
        isPeriodic = false
    }

    constructor(
        source: String?, destination: String?, periodicity: Double, tupleCpuLength: Double,
        tupleNwLength: Double, tupleType: String?, direction: Int, edgeType: Int
    ) {
        this.source = source
        this.destination = destination
        this.tupleCpuLength = tupleCpuLength
        this.tupleNwLength = tupleNwLength
        this.tupleType = tupleType
        this.direction = direction
        this.edgeType = edgeType
        this.isPeriodic = true
        this.periodicity = periodicity
    }

    override fun toString(): String {
        return ("AppEdge [source=" + source + ", destination=" + destination
                + ", tupleType=" + tupleType + "]")
    }

    companion object {
        const val SENSOR = 1 // App Edge originates from a sensor
        const val ACTUATOR = 2 // App Edge leads to an actuator
        const val MODULE = 3 // App Edge is between application modules
    }
}
