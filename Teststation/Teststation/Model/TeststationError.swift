import Foundation

enum TeststationError: Error {
    case statusCode
    case notFound
    case encoding
    case decoding
    case invalidImage
    case invalidURL
    case unknown
    case other(Error)
    
    static func map(_ error: Error) -> TeststationError {
        print("TeststationError.map(error=\(error))")
        
        return (error as? TeststationError) ?? .other(error)
    }
    
    static func map(_ response: URLResponse) -> TeststationError  {
        var result = Self.unknown
        
        guard
            let httpURLResponse = response as? HTTPURLResponse
        else {
            return result
        }
        
        switch httpURLResponse.statusCode {
        case 404:
            result = Self.notFound
        default:
            result = Self.statusCode
        }
        
        return result
    }
}
