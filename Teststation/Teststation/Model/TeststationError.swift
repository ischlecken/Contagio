import Foundation

enum TeststationError: Error {
  case statusCode
  case decoding
  case invalidImage
  case invalidURL
  case other(Error)
  
  static func map(_ error: Error) -> TeststationError {
    return (error as? TeststationError) ?? .other(error)
  }
}
