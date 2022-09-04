const winston = require('winston');
const {format, createLogger, transports} = winston;

const logger = createLogger({
    format: format.combine(
        format.timestamp({
            format: 'YYYY-MM-DD HH:mm:ss'
        }),
        //
        // Alternatively you could use this custom printf format if you
        // want to control where the timestamp comes in your final message.
        // Try replacing `format.simple()` above with this:
        //
        format.printf(info => `[${info.level}] ${info.timestamp} - ${info.message}`)
    ),
    transports: [new transports.Console()]
});

module.exports = logger;